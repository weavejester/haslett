(defproject haslett "0.1.0-SNAPSHOT"
  :description "A lightweight WebSocket library for ClojureScript"
  :url "https://github.com/weavejester/haslett"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.562"]
                 [org.clojure/core.async "0.3.443"]
                 [com.cognitect/transit-cljs "0.8.239"]]
  :plugins [[lein-cljsbuild "1.1.6"]]
  :aliases {"test" ["run" "-m" "haslett.test-runner"]}
  :profiles
  {:dev {:dependencies [[doo "0.1.7"]
                        [http-kit "2.2.0"]]
         :prep-tasks   ["compile" ["cljsbuild" "once"]]
         :cljsbuild
         {:builds
          {:test
           {:source-paths ["src" "test"]
            :compiler {:output-to "target/main.js"
                       :output-dir "target"
                       :main haslett.test-runner
                       :optimizations :none}}}}}})
