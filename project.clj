(defproject haslett "0.2.0"
  :description "A lightweight WebSocket library for ClojureScript"
  :url "https://github.com/weavejester/haslett"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.11.3"]
                 [org.clojure/clojurescript "1.11.132"]
                 [org.clojure/core.async "1.6.681"]
                 [com.cognitect/transit-cljs "0.8.280"]]
  :plugins [[lein-cljsbuild "1.1.8"]]
  :aliases {"test" ["run" "-m" "haslett.test-runner"]}
  :profiles
  {:dev {:dependencies [[doo "0.1.11"]
                        [http-kit "2.8.0"]]
         :prep-tasks   ["compile" ["cljsbuild" "once"]]
         :cljsbuild
         {:builds
          {:test
           {:source-paths ["src" "test"]
            :compiler {:output-to "target/main.js"
                       :output-dir "target"
                       :main haslett.test-runner
                       :optimizations :none}}}}}})
