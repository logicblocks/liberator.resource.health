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

(defn dependency-check [check-name check-fn]
  {:name     check-name
   :check-fn check-fn})

(defn definitions
  ([dependencies] (definitions dependencies {}))
  ([{:keys [routes] :as dependencies}
    {:keys [version-file-path
            dependency-checks]}]
   {:initialize-context
    (fn [_]
      (let [dependency-results
            (reduce
              (fn [dependency-results {:keys [name check-fn]}]
                (assoc dependency-results
                  name (check-fn dependencies)))
              nil
              dependency-checks)]
        (cond-> {}
          (some? version-file-path)
          (merge {:version (read-version version-file-path)})

          (some? dependency-results)
          (merge {:dependency-results dependency-results}))))

    :handle-ok
    (fn [{:keys [request version dependency-results]}]
      (cond-> (hal/new-resource
                (hype/absolute-url-for request routes :health))
        (some? version)
        (hal/add-property :version version)

        (some? dependency-results)
        (hal/add-property :dependencies dependency-results)))}))

(defn handler
  ([dependencies] (handler dependencies {}))
  ([dependencies options]
   (mixin/build-resource
     (json-mixin/with-json-mixin dependencies)
     (hypermedia-mixin/with-hypermedia-mixin dependencies)
     (hal-mixin/with-hal-mixin dependencies)
     (definitions dependencies options))))
