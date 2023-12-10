
(ns source-code-documentation.map.engine
    (:require [fruits.regex.api                      :as regex]
              [fruits.vector.api                     :as vector]
              [io.api                                :as io]
              [source-code-documentation.core.config :as core.config]
              [source-code-map.api                   :as source-code-map]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn map-source-paths
  ; @ignore
  ;
  ; @description
  ; - Maps namespaces from all CLJ, CLJC, and CLJS source files within the given source directories.
  ; - Although the documentation generator creates documentation only for files that match the provided (or default)
  ;   filename pattern, to handle header redirections, the documentation generator requires mapping of namespaces in all available source files.
  ;
  ; @param (vector) state
  ; @param (map) options
  ;
  ; @example
  ; (map-source-paths [] {:filename-pattern #"my\_namespace\_a\.clj"
  ;                       :source-paths ["source-code"]})
  ; =>
  ; [{:filepath "source-code/my_namespace_a.clj" :ns-map {...} :create-documentation? true}
  ;  {:filepath "source-code/my_namespace_b.clj" :ns-map {...} :create-documentation? false}
  ;  {:filepath "source-code/my_namespace_c.clj" :ns-map {...} :create-documentation? false}]
  ;
  ; @return (maps in vector)
  [state {:keys [filename-pattern source-paths]}]
  (letfn [(f0 [filepath]    (-> filepath io/filepath->filename (regex/re-match? filename-pattern)))
          (f1 [source-path] (io/search-files source-path core.config/SOURCE-FILENAME-PATTERN))
          (f2 [filepath]    {:filepath filepath :ns-map (source-code-map/read-ns-map filepath) :create-documentation? (f0 filepath)})]
         (-> source-paths (vector/->items f1)
                          (vector/flat-items)
                          (vector/->items f2))))
