(ns tgm-ai.core
  (:use [clisk live])
  (:require [tweegeemee.core :as tgm]
            [clojure.tools.cli :refer [parse-opts]]
            ;;[clojure.pprint :as pprint]
            [clojure.data.json :as json]
            [clojure.java.io :as io])
  (:import [java.io File]
           [javax.imageio ImageIO])
  (:gen-class))

(defn get-score
  "get the AI to score an image"
  [filename]
  (let [url (format "http://localhost:8484/ai/v1/score?file=%s" filename)
        rsp (json/read-str (slurp url))]
    (rsp "yes")))

(defn create-random-image-tuple
  "create a new random image tuple containing :code, :image, :score"
  [idx dir]
  (let [filename (format "%s/img%03d.png" dir idx)
        _        (println "\n" filename)
        code     (tgm/get-random-code)
        image    (clisk.live/image (eval code) :size 224)
        _        (ImageIO/write image "png" (File. filename))
        score    (get-score filename)]
    {:code  code
     :image filename
     :score score}))

(defn create-random-images
  "update image-list with num-random-images & scores"
  [image-list num-random-images image-dir]
  (reset! image-list
          (map #(create-random-image-tuple % image-dir) (range num-random-images))))

(defn create-child-image-tuple
  "create a new random image tuple containing :code, :image, :score"
  [idx dir parent0 parent1]
  (let [parent0-code (parent0 :code)
        parent1-code (parent1 :code)
        filename (format "%s/img%03d.png" dir idx)
        _        (println "\n" filename)
        code     (tgm/get-random-child parent0-code parent1-code)
        image    (clisk.live/image (eval code) :size 224)
        _        (ImageIO/write image "png" (File. filename))
        score    (get-score filename)]
    {:code  code
     :image filename
     :score score}))

(defn breed-an-image
  "update image-list with a new image"
  [image-list image-dir]
  (let [_       (reset! image-list (sort-by :score #(compare %2 %1) @image-list))
        num-images (count @image-list)
        n       (max 3 (int (Math/floor (* 0.1 num-images))))
        parents (take n @image-list)
        parent0 (rand-nth parents)
        parent1 (rand-nth parents)
        child   (create-child-image-tuple num-images image-dir parent0 parent1)]
    (swap! image-list conj child)))

(defn breed-images
  "breed top-scoring images and update image-list"
  [image-list num-total-images image-dir]
  (while (< (count @image-list) num-total-images)
    (breed-an-image image-list image-dir)))

(defn report-images
  "output some info on image-list"
  [image-list]
  (reset! image-list (sort-by :score #(compare %2 %1) @image-list))
  (println "\nImage & Score")
  (dorun (map #(println (% :image) (% :score)) @image-list)))

(defn run
  "run the algorithm to breed images"
  [num-random-images num-total-images image-dir]
  (let [image-list (atom [])]
    ;; image-list is state & contains :code, :image, :score
    ;; reverse-sorted by :score.
    (create-random-images image-list num-random-images image-dir)
    (breed-images image-list num-total-images image-dir)
    (report-images image-list)))

(def args-spec [["-h" "--help" "Print this help" :default false]
                ["-r" "--num-random-images R" "Number of initial random images to create" :parse-fn #(Integer/parseInt %) :default 5]
                ["-t" "--num-total-images T" "Number of total images to create" :parse-fn #(Integer/parseInt %) :default 10]
                ["-o" "--output-path PATH" "Directory to store the images" :default "images/00" ]
                ])

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [_ (println args)
        {:keys [options arguments summary errors]} (parse-opts args args-spec)
        num-random-images (:num-random-images options)
        num-total-images  (:num-total-images options)
        image-dir         (:output-path options)]
    (println options)
    (io/make-parents (str image-dir "/foo.txt"))
    (if (:help options)
      (println summary)
      ;; else
      (do (println "tweegeemee ai image creation")
          (println "num random images: " num-random-images)
          (println "num total images:  " num-total-images)
          (println "image directory:   " image-dir)
          (println)
          (run num-random-images num-total-images image-dir)
          (shutdown-agents)))))
