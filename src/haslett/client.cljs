(ns haslett.client
  "A namespace for opening WebSockets in ClojureScript."
  (:require [cljs.core.async :as a :refer [<! >!]]
            [haslett.format :as fmt])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn connect
  "Create a WebSocket to the specified URL, and returns a 'stream' map of three
  keys:

    :socket - contains the WebSocket object
    :source - a core.async channel to read from
    :sink   - a core.async channel to write to

  Takes the following options:

    :format      - a formatter from haslett.format
    :source      - a custom channel to use as the source
    :sink        - a custom channel to use as the sink
    :binary-type - passed to the WebSocket, may be :blob or :arraybuffer"
  ([url]
   (connect url {}))
  ([url options]
   (let [socket (js/WebSocket. url)
         source (:source options (a/chan))
         sink   (:sink   options (a/chan))
         format (:format options fmt/identity)
         stream {:socket socket, :source source, :sink sink}
         return (a/promise-chan)]
     (set! (.-binaryType socket) (name (:binary-type options :arraybuffer)))
     (set! (.-onopen socket)     (fn [_] (a/put! return stream)))
     (set! (.-onclose socket)    (fn [_] (a/close! source) (a/close! sink)))
     (set! (.-onmessage socket)  (fn [e] (a/put! source (fmt/read format (.-data e)))))
     (go-loop []
       (when-let [msg (<! sink)]
         (.send socket (fmt/write format msg))
         (recur)))
     return)))

(defn close
  "Close a stream opened by connect."
  [stream]
  (.close (:socket stream)))
