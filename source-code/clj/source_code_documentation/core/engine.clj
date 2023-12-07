
(ns source-code-documentation.core.engine
    (:require [source-code-documentation.core.prototypes :as core.prototypes]
              [source-code-documentation.core.patterns :as core.patterns]
              [source-code-documentation.import.engine :as import.engine]
              [source-code-documentation.read.engine :as read.engine]
              [source-code-documentation.map.engine :as map.engine]
              [validator.api :as v]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn generate-documentation!
  ; @warning
  ; The 'generate-documentation!' function deletes the output directory before exporting the documentation files!
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
  ;                           :code-dirs        ["submodules/my-repository/source-code"]
  ;                           :filename-pattern "[a-z\-]\.clj"
  ;                           :output-dir       "submodules/my-repository/documentation"
  ;                           :lib-name         "My library"
  ;                           :website          "https://github.com/author/my-repository"})
  ;
  ; @return (?)
  [options]
  (if (v/valid? options {:pattern* core.patterns/OPTIONS-PATTERN})
      (let [options (core.prototypes/options-prototype options)]
           (-> {} (map.engine/map-source-paths options)
                  (import.engine/import-source-files options)
                  (read.engine/read-imported-files   options)))))
