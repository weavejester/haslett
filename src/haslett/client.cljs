(ns haslett.client
  "A namespace for opening WebSockets in ClojureScript."
  (:require [cljs.core.async :as a :refer [<! >!]]
            [haslett.format :as fmt])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn connect
  "Create a WebSocket to the specified URL, and returns a 'stream' map of four
  keys:

    :socket       - contains the WebSocket object
    :close-status - a promise channel that contains the final close status
    :source       - a core.async channel to read from
    :sink         - a core.async channel to write to

  Takes the following options:

    :format      - a formatter from haslett.format
    :source      - a custom channel to use as the source
    :sink        - a custom channel to use as the sink
    :protocols   - passed to the WebSocket, a vector of protocol strings
    :binary-type - passed to the WebSocket, may be :blob or :arraybuffer
    :close-chan? - true if channels should be closed if WebSocket is closed
                   (defaults to true)

  The WebSocket may either be closed directly, or by closing the
  stream's :sink channel."
  ([url]
   (connect url {}))
  ([url options]
   (let [protocols (into-array (:protocols options []))
         socket    (js/WebSocket. url protocols)
         source    (:source options (a/chan))
         sink      (:sink   options (a/chan))
         format    (:format options fmt/identity)
         status    (a/promise-chan)
         return    (a/promise-chan)
         close?    (:close-chan? options true)
         stream    {:socket socket, :source source, :sink sink, :close-status status}]
     (set! (.-binaryType socket) (name (:binary-type options :arraybuffer)))
     (set! (.-onopen socket)     (fn [_] (a/put! return stream)))
     (set! (.-onmessage socket)  (fn [e] (a/put! source (fmt/read format (.-data e)))))
     (set! (.-onclose socket)    (fn [e]
                                   (a/put! status {:reason (.-reason e), :code (.-code e)})
                                   (when close? (a/close! source))
                                   (when close? (a/close! sink))
                                   (a/put! return stream)))
     (go-loop []
       (when-let [msg (<! sink)]
         (.send socket (fmt/write format msg))
         (recur))
       (close stream))
     return)))

(defn close
  "Close a stream opened by connect."
  [stream]
  (.close (:socket stream) 1000 "Closed by creator")
  (:close-status stream))

(defn connected?
  "Return true if the stream is currently connected."
  [{:keys [close-status]}]
  (nil? (a/poll! close-status)))
