
(ns source-code-documentation.map.engine
    (:require [source-code-map.api :as source-code-map]
              [io.api :as io]
              [map.api :as map]
              [vector.api :as vector]
              [regex.api :as regex]
              [source-code-documentation.core.config :as core.config]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn map-source-paths
  ; @ignore
  ;
  ; @description
  ; - Maps namespaces from all CLJ, CLJC, and CLJS source files within the given source directories.
  ; - Although the documentation generator creates documentation only for files that match the provided (or default)
  ;   filename pattern, to handle symbol redirections, the documentation generator requires mapping for all available source files.
  ;
  ; @param (map) state
  ; @param (map) options
  ;
  ; @return (map)
  [state {:keys [filename-pattern source-paths]}]
  (letfn [(f0 [filename]    (regex/re-match? filename filename-pattern))
          (f1 [source-path] (io/search-files source-path core.config/DEFAULT-FILENAME-PATTERN))
          (f2 [_ filepath]  [filepath {:ns-map (source-code-map/read-ns-map filepath)
                                       :create-documentation? (f0 filepath)}])]
         (-> source-paths (vector/->items f1)
                          (vector/flat-items)
                          (vector/to-map f2))))
