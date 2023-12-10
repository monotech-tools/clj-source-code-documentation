
(ns source-code-documentation.core.engine
    (:require [source-code-documentation.core.patterns   :as core.patterns]
              [source-code-documentation.core.prototypes :as core.prototypes]
              [source-code-documentation.import.engine   :as import.engine]
              [source-code-documentation.map.engine      :as map.engine]
              [source-code-documentation.read.engine     :as read.engine]
              [validator.api                             :as v]
              [source-code-documentation.trace.engine :as trace.engine]
              [source-code-documentation.process.engine :as process.engine]
              [source-code-documentation.resolve.engine :as resolve.engine]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn generate-documentation!
  ; @warning
  ; The 'generate-documentation!' function ereases the output directory before exporting the documentation files!
  ;
  ; @param (map) options
  ; {:author (string)(opt)
  ;  :filename-pattern (regex pattern)(opt)
  ;   Default: #"[a-z\_\d]{1,}\.clj[cs]{0,1}"
  ;  :lib-name (string)
  ;  :output-path (string)
  ;  :previews-path (string)(opt)
  ;  :print-format (keyword)(opt)
  ;   :html, :md
  ;   Default: :md
  ;  :print-options (keywords in vector)(opt)
  ;   [:code, :credit, :description, :example, :param, :preview, :require, :return, :usage, :warning]
  ;   Default: [:code :credit :description :example :param :preview :require :return :usage :warning]
  ;  :source-paths (strings in vector)
  ;  :website (string)(opt)}
  ;
  ; @usage
  ; (generate-documentation! {...})
  ;
  ; @usage
  ; (generate-documentation! {:author           "Author"
  ;                           :filename-pattern "[a-z\_]\.clj"
  ;                           :lib-name         "My library"
  ;                           :output-path      "documentation"
  ;                           :source-paths     ["source-code"]
  ;                           :website          "https://github.com/author/my-repository"})
  ;
  ; @return (?)
  [options]
  (if (v/valid? options {:pattern* core.patterns/OPTIONS-PATTERN})
      (let [options (core.prototypes/options-prototype   options)]
           (-> [] (map.engine/map-source-paths           options)
                  (import.engine/import-source-files     options)
                  (read.engine/read-imported-files       options)
                  (trace.engine/trace-imported-files     options)
                  (process.engine/process-imported-files options)
                  (resolve.engine/resolve-imported-files options)))))
