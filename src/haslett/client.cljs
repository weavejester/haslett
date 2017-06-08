(ns haslett.client
  (:require [cljs.core.async :as a :refer [<! >!]]
            [cljs.reader :as edn]
            [cognitect.transit :as transit])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn- chan-fn [xf]
  (fn chan
    ([]                          (chan 1))
    ([buf-or-n]                  (chan buf-or-n identity))
    ([buf-or-n xform]            (chan buf-or-n xform nil))
    ([buf-or-n xform ex-handler] (a/chan buf-or-n (comp xf xform) ex-handler))))

(def transit-source (chan-fn (map #(transit/read (transit/reader :json) %))))
(def transit-sink   (chan-fn (map #(transit/write (transit/writer :json) %))))

(def edn-source     (chan-fn (map edn/read-string)))
(def edn-sink       (chan-fn (map pr-str)))

(def json-source    (chan-fn (map #(js->clj (js/JSON.parse %)))))
(def json-sink      (chan-fn (map #(js/JSON.stringify (clj->js %)))))

(defn connect
  ([url]
   (connect url {}))
  ([url options]
   (let [socket (js/WebSocket. url)
         source (:source options (transit-source))
         sink   (:sink options   (transit-sink))
         stream {:socket socket, :source source, :sink sink}
         return (a/promise-chan)]
     (set! (.-binaryType socket) (:binary-type options "arraybuffer"))
     (set! (.-onopen socket)     (fn [_] (a/put! return stream)))
     (set! (.-onclose socket)    (fn [_] (a/close! source) (a/close! sink)))
     (set! (.-onmessage socket)  (fn [e] (a/put! source (.-data e))))
     (go-loop []
       (if-let [msg (<! sink)]
         (do (.send socket msg) (recur))
         (close socket)))
     return)))

(defn close [stream]
  (.close (:socket stream)))
