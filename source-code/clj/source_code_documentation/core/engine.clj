
(ns source-code-documentation.core.engine
    (:require [source-code-documentation.core.tests :as core.tests]
              [source-code-documentation.core.prototypes :as core.prototypes]
              [source-code-documentation.import.engine   :as import.engine]
              [source-code-documentation.map.engine      :as map.engine]
              [source-code-documentation.read.engine     :as read.engine]
              [validator.api                             :as v]
              [source-code-documentation.trace.engine :as trace.engine]
              [source-code-documentation.resolve.engine :as resolve.engine]
              [source-code-documentation.print.engine :as print.engine]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn generate-documentation!
  ; @warning
  ; The 'generate-documentation!' function ereases the output directory before exporting the documentation files!
  ;
  ; @param (map) options
  ; {:author (map)(opt)
  ;   {:name (string)(opt)
  ;    :website (string)(opt)}
  ;  :filename-pattern (regex pattern)(opt)
  ;   Default: #"[a-z\_\d]{1,}\.clj[cs]{0,1}"
  ;  :library (map)(opt)}
  ;   {:name (string)(opt)
  ;    :website (string)(opt)}
  ;  :output-path (string)
  ;  :previews-path (string)(opt)
  ;  :print-format (keyword)(opt)
  ;   :html, :md
  ;   Default: :html
  ;  :print-options (keywords in vector)(opt)
  ;   [:code, :credit, :description, :example, :param, :preview, :require, :return, :usage, :warning]
  ;   Default: [:code :credit :description :example :param :preview :require :return :usage :warning]
  ;  :source-paths (strings in vector)
  ;  :version (string)(opt)}
  ;
  ; @usage
  ; (generate-documentation! {...})
  ;
  ; @usage
  ; (generate-documentation! {:author           {:name "Author" :website "https://author.com"}
  ;                           :filename-pattern "[a-z\_]\.clj"
  ;                           :library          {:name "My library" :website"https://github.com/author/my-repository"}
  ;                           :output-path      "documentation"
  ;                           :source-paths     ["source-code"]
  ;                           :version          "1.0.0.0"})
  ;
  ; @return (?)
  [options]
  (if (v/valid? options core.tests/OPTIONS-TEST {:prefix "options"})
      (let [options (core.prototypes/options-prototype   options)]
           (-> [] (map.engine/map-source-paths           options)
                  (import.engine/import-source-files     options)
                  (read.engine/read-imported-files       options)
                  (trace.engine/trace-imported-files     options)
                  (resolve.engine/resolve-imported-files options)
                  (print.engine/print-documentation!     options)))))
