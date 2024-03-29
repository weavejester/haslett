# Haslett

[![Build Status](https://travis-ci.org/weavejester/haslett.svg?branch=master)](https://travis-ci.org/weavejester/haslett)

A lightweight WebSocket library for ClojureScript that uses
[core.async][].

[core.async]: https://github.com/clojure/core.async

## Installation

Add the following dependency to your `project.clj` file:

    [haslett "0.1.7"]

## Usage

Haslett provides a simple and idiomatic interface to using WebSockets:

```clojure
(ns example.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as a :refer [<! >!]]
            [haslett.client :as ws]
            [haslett.format :as fmt]))

(go (let [stream (<! (ws/connect "ws://echo.websocket.org"))]
      (>! (:sink stream) "Hello World")
      (js/console.log (<! (:source stream)))
      (ws/close stream)))
```

The `connect` function returns a promise channel that produces a map
with four keys: `:socket`, `:close-status`, `:source` and `:sink`.

* `:socket` contains the JavaScript `WebSocket` object, in case you need
to access it directly.

* `:close-status` contains a promise channel that a status map is
delivered to when the socket is closed. The status map will provide a
`:code` and `:reason` keys that will explain why the socket was
closed.

* `:source` is a core.async channel to read from.

* `:sink` is a core.async channel to write to.

By default, Haslett sends raw strings, but we can change that by
supplying a formatter. Haslett includes formatters for JSON, edn and
Transit:

```clojure
(go (let [stream (<! (ws/connect "ws://echo.websocket.org" {:format fmt/transit}))]
      (>! (:sink stream) {:foo [1 2 3]})
      (js/console.log (pr-str (<! (:source stream))))
      (ws/close stream)))
```

You can customize the behaviour further by supplying your own channels
for the source and sink. This allows you to tune the channel buffer,
and add tranducers:

```clojure
(ws/connect "ws://echo.websocket.org"
            {:source (a/chan 10)
             :sink   (a/chan 10)})
```

When the WebSocket is closed, the `:sink` and `:source` channels are
also closed. In addition, a final status map will be delivered to a
promise channel held in the `:close-status` key on the stream.

## License

Copyright © 2022 James Reeves

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
