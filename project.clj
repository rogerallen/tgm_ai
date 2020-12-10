(defproject tgm_ai "1.0.0"
  :description "tweegeemee ai"
  :url "http://no.website.yet/"
  :license {:name "EPL-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.cli "1.0.194"]
                 [tweegeemee "1.5.0"]]
  :main ^:skip-aot tgm-ai.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
