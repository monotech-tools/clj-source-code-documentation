
(ns source-code-documentation.core.prototypes
    (:require [fruits.uri.api                        :as uri]
              [fruits.vector.api                     :as vector]
              [fruits.map.api :as map]
              [io.api                                :as io]
              [source-code-documentation.core.config :as core.config]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn options-prototype
  ; @ignore
  ;
  ; @param (map) options
  ;
  ; @return (map)
  ; {:base-uri (string)
  ;  :filename-pattern (regex pattern)
  ;  :output-path (string)
  ;  :previews-uri (string)
  ;  :snippet-config (map)
  ;  :source-paths (strings in vector)
  ;  ...}
  [options]
  (merge {:filename-pattern core.config/DEFAULT-FILENAME-PATTERN}
         (-> options (update :base-uri      uri/valid-url)
                     (update :output-path   io/valid-absolute-path)
                     (update :previews-uri  uri/valid-url)
                     (update :source-paths  vector/->items io/valid-absolute-path)
                     (update :snippet-config map/reversed-merge core.config/PREDEFINED-SNIPPET-CONFIG))))
