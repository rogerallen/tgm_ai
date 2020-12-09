(ns tgm-ai.core
  (:use [clisk live])
  (:require [tweegeemee.core :as tgmc]
            [tweegeemee.image :as tgmi]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.pprint :as pprint]
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
    (rsp "score")))

(defn create-random-image-tuple
  "create a new random image tuple containing :code, :image, :score"
  [idx dir]
  (let [filename   (format "%s/img%03d.png" dir idx)
        _          (println "\n" filename)
        code       (tgmc/get-random-code)
        code-hash  (hash code)
        image-hash (tgmi/image-hash (clisk.live/image (eval code) :size tgmi/TEST-IMAGE-SIZE))
        image      (clisk.live/image (eval code) :size 224)
        _          (ImageIO/write image "png" (File. filename))
        score      (get-score filename)]
    {:code  code
     :code-hash code-hash
     :image filename
     :image-hash image-hash
     :score score
     :type "random"}))

(defn create-child-image-tuple
  "create a new child image tuple containing :code, :image, :score"
  [idx dir parent0 parent1]
  (let [parent0-code (parent0 :code)
        parent1-code (parent1 :code)
        filename     (format "%s/img%03d.png" dir idx)
        _            (println "\n" filename)
        code         (tgmc/get-random-child parent0-code parent1-code)
        code-hash    (hash code)
        image-hash   (tgmi/image-hash (clisk.live/image (eval code) :size tgmi/TEST-IMAGE-SIZE))
        image        (clisk.live/image (eval code) :size 224)
        _            (ImageIO/write image "png" (File. filename))
        score        (get-score filename)]
    {:code  code
     :code-hash code-hash
     :image filename
     :image-hash image-hash
     :score score
     :type "child"}))

(defn create-mutant-image-tuple
  "create a new mutant image tuple containing :code, :image, :score"
  [idx dir parent]
  (let [parent-code (parent :code)
        filename    (format "%s/img%03d.png" dir idx)
        _           (println "\n" filename)
        code        (tgmc/get-random-mutant parent-code)
        code-hash   (hash code)
        image-hash  (tgmi/image-hash (clisk.live/image (eval code) :size tgmi/TEST-IMAGE-SIZE))
        image       (clisk.live/image (eval code) :size 224)
        _           (ImageIO/write image "png" (File. filename))
        score       (get-score filename)]
    {:code  code
     :code-hash code-hash
     :image filename
     :image-hash image-hash
     :score score
     :type "mutant"}))

(defn score-to-weight
  "convert score to good weight for that image"
  [score]
  (let [weight (* 10 score)
        weight (int (* weight weight))]
    weight))

(defn wrand
  "given a vector of slice sizes, returns the index of a slice given a
  random spin of a roulette wheel with compartments proportional to
  slices."
  [slices]
  (let [total (reduce + slices)
        r     (rand total)]
    (loop [i 0 sum 0]
      (if (< r (+ (slices i) sum))
        i
        (recur (inc i) (+ (slices i) sum))))))

(defn image-tuple-match
  "check the :code-hash and :image-hash to see if these
   tuples are creating the same image"
  [img1 img2]
  (or (= (img1 :code-hash) (img2 :code-hash))
      (= (img1 :image-hash) (img2 :image-hash))))

(defn contains-image? 
  "return true/false if image is in @image-list"
  [image-list image]
  (some #(image-tuple-match % image) @image-list))

(defn create-random-image! 
  "update image-list with a new random image"
  [image-list image-dir]
  (loop []
    (let [_ (println ">>>>>random #" (count @image-list))
          new-image (create-random-image-tuple (count @image-list) image-dir)]
      (if (not (contains-image? image-list new-image))
        (swap! image-list conj new-image)
        (recur))))) 

(defn breed-an-image!
  "update image-list with a new image"
  [image-list image-dir]
  (loop []
    (let [_ (println ">>>>>breed #" (count @image-list))
          _          (reset! image-list (sort-by :score #(compare %2 %1) @image-list))
          num-images (count @image-list)
        ;;_          (println num-images "num-images")
        ;;n        (min 10 (max 5 (int (Math/floor (* 0.25 num-images)))))
          parents    (take-while #(> (:score %) 0.0) @image-list)
        ;;_        (pprint/pprint (map #(dissoc % :code) parents))
          _          (println (count parents) "parents")
          weights    (doall (mapv #(score-to-weight (:score %)) parents))
        ;;_          (println "weights:" weights)
          parent0    (nth parents (wrand weights))
          parent1    (nth parents (wrand weights))
          child      (if (< (rand) 0.5)
                       (create-child-image-tuple num-images image-dir parent0 parent1)
                       (create-mutant-image-tuple num-images image-dir parent0))]
      (if (not (contains-image? image-list child))
        (swap! image-list conj child)
        (recur)))))

(defn create-random-images
  "update image-list with num-random-images & scores"
  [image-list num-random-images image-dir]
  (while (< (count @image-list) num-random-images)
    (create-random-image! image-list image-dir)))

(defn breed-images
  "breed top-scoring images and update image-list"
  [image-list num-total-images image-dir]
  (while (< (count @image-list) num-total-images)
    (breed-an-image! image-list image-dir)))

(defn report-images
  "output some info on image-list"
  [image-list image-dir]
  (reset! image-list (sort-by :score @image-list))
  (println "\nImage & Score")
  (dorun (map #(println (% :image) (% :score)) @image-list))
  (pprint/pprint @image-list (io/writer (str image-dir "/report.txt"))))

(defn run
  "run the algorithm to breed images"
  [num-random-images num-total-images image-dir]
  (let [image-list (atom [])]
    ;; image-list is state & contains :code, :image, :score
    ;; reverse-sorted by :score.
    (create-random-images image-list num-random-images image-dir)
    (breed-images image-list num-total-images image-dir)
    (report-images image-list image-dir)))

(def args-spec [["-h" "--help" "Print this help" :default false]
                ["-r" "--num-random-images R" "Number of initial random images to create" :parse-fn #(Integer/parseInt %) :default 5]
                ["-t" "--num-total-images T" "Number of total images to create" :parse-fn #(Integer/parseInt %) :default 10]
                ["-o" "--output-path PATH" "Directory to store the images" :default "images/00"]])

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
