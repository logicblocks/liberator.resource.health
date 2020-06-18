(ns liberator-hal.health-resource.core
  (:require
   [liberator-mixin.core :as mixin]
   [liberator-mixin.json.core :as json-mixin]
   [liberator-mixin.hypermedia.core :as hypermedia-mixin]
   [liberator-mixin.hal.core :as hal-mixin]
   [halboy.resource :as hal]
   [hype.core :as hype]))

(defn definitions
  ([dependencies] (definitions dependencies {}))
  ([{:keys [routes]}
    _]
   {:handle-ok
    (fn [{:keys [request]}]
      (hal/new-resource
        (hype/absolute-url-for request routes :health)))}))

(defn handler
  ([dependencies] (handler dependencies {}))
  ([dependencies options]
   (mixin/build-resource
     (json-mixin/with-json-mixin dependencies)
     (hypermedia-mixin/with-hypermedia-mixin dependencies)
     (hal-mixin/with-hal-mixin dependencies)
     (definitions dependencies options))))
