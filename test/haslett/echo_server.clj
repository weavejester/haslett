(ns haslett.echo-server
  (:require [org.httpkit.server :as httpkit]))

(defn echo-handler [request]
  (httpkit/with-channel request channel
    (httpkit/on-receive channel (fn [data] (httpkit/send! channel data)))))

(defn -main []
  (httpkit/run-server echo-handler {:port 3200}))
