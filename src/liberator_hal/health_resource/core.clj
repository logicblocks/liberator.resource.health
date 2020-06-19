(ns liberator-hal.health-resource.core
  (:require
   [clojure.string :as string]
   [halboy.resource :as hal]
   [hype.core :as hype]

   [liberator-mixin.core :as mixin]
   [liberator-mixin.json.core :as json-mixin]
   [liberator-mixin.hypermedia.core :as hypermedia-mixin]
   [liberator-mixin.hal.core :as hal-mixin])
  (:import
   [java.io FileNotFoundException]))

(defn- read-version [path]
  (try
    (string/trim (slurp path))
    (catch FileNotFoundException _
      "missing")))

(defn definitions
  ([dependencies] (definitions dependencies {}))
  ([{:keys [routes]}
    {:keys [version-file-path]}]
   {:initialize-context
    (fn [_]
      (when version-file-path
        {:version (read-version version-file-path)}))

    :handle-ok
    (fn [{:keys [request version]}]
      (cond->
        (hal/new-resource
          (hype/absolute-url-for request routes :health))
        (some? version) (hal/add-property :version version)))}))

(defn handler
  ([dependencies] (handler dependencies {}))
  ([dependencies options]
   (mixin/build-resource
     (json-mixin/with-json-mixin dependencies)
     (hypermedia-mixin/with-hypermedia-mixin dependencies)
     (hal-mixin/with-hal-mixin dependencies)
     (definitions dependencies options))))
