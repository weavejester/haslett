(ns haslett.test-runner
  (:require [doo.core :as doo]
            [org.httpkit.server :as httpkit]))

(def compiler-opts
  {:output-to "target/main.js"
   :output-dir "target"
   :main 'haslett.test-runner
   :optimizations :none})

(defn echo-handler [request]
  (httpkit/with-channel request channel
    (httpkit/on-receive channel (fn [data] (httpkit/send! channel data)))))

(defn run-server []
  (httpkit/run-server echo-handler {:port 3200}))

(defn run-tests []
  (doo/run-script :phantom compiler-opts))

(defn -main []
  (println "Starting server")
  (let [stop-server (run-server)]
    (println "Running tests")
    (run-tests)
    (println "Stopping server")
    (stop-server)
    (shutdown-agents)))
