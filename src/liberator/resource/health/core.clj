(ns liberator.resource.health.core
  (:require
   [clojure.string :as string]
   [halboy.resource :as hal]
   [hype.core :as hype]

   [liberator.mixin.core :as mixin]
   [liberator.mixin.json.core :as json-mixin]
   [liberator.mixin.hypermedia.core :as hypermedia-mixin]
   [liberator.mixin.hal.core :as hal-mixin])
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
  ([dependencies]
   {:initialize-context
    (fn [{:keys [resource] :as context}]
      (let [version-file-path-fn (:version-file-path resource)
            version-file-path (and version-file-path-fn
                                (version-file-path-fn context))

            dependency-checks-fn (:dependency-checks resource)
            dependency-checks (and dependency-checks-fn
                                (dependency-checks-fn context))

            dependency-results
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
    (fn [{:keys [request router version dependency-results]}]
      (cond-> (hal/new-resource
                (hype/absolute-url-for request router :health))
        (some? version)
        (hal/add-property :version version)

        (some? dependency-results)
        (hal/add-property :dependencies dependency-results)))}))

(defn handler
  ([dependencies] (handler dependencies {}))
  ([dependencies overrides]
   (mixin/build-resource
     (json-mixin/with-json-mixin dependencies)
     (hypermedia-mixin/with-hypermedia-mixin dependencies)
     (hal-mixin/with-hal-mixin dependencies)
     (definitions dependencies)
     overrides)))
