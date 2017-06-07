(ns haslett.client
  (:refer-clojure :exclude [send]))

(defn websocket
  ([url]
   (websocket url {}))
  ([url options]
   (let [sock (js/WebSocket. url)]
     (when-let [on-open    (:on-open options)]
       (aset sock "onopen"    (fn [evt] (on-open sock evt))))
     (when-let [on-close   (:on-close options)]
       (aset sock "onclose"   (fn [evt] (on-close sock evt))))
     (when-let [on-message (:on-message options)]
       (aset sock "onmessage" (fn [evt] (on-message sock (.-data evt) evt))))
     (when-let [on-error   (:on-error options)]
       (aset sock "onerror"   (fn [evt] (on-error sock evt))))
     sock)))

(defn send [ws data]
  (.send ws data))

(defn close [ws]
  (.close ws))
