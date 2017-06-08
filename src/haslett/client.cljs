(ns haslett.client
  (:require [cljs.core.async :as a :refer [<! >!]]
            [haslett.format :as fmt])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn connect
  ([url]
   (connect url {}))
  ([url options]
   (let [socket (js/WebSocket. url)
         source (:source options (a/chan))
         sink   (:sink   options (a/chan))
         format (:format options fmt/identity)
         stream {:socket socket, :source source, :sink sink}
         return (a/promise-chan)]
     (set! (.-binaryType socket) (:binary-type options "arraybuffer"))
     (set! (.-onopen socket)     (fn [_] (a/put! return stream)))
     (set! (.-onclose socket)    (fn [_] (a/close! source) (a/close! sink)))
     (set! (.-onmessage socket)  (fn [e] (a/put! source (fmt/read format (.-data e)))))
     (go-loop []
       (if-let [msg (<! sink)]
         (do (.send socket (fmt/write format msg)) (recur))
         (close socket)))
     return)))

(defn close [stream]
  (.close (:socket stream)))
