
(ns source-code-documentation.core.tests
    (:require [fruits.regex.api :as regex]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @description
; https://github.com/bithandshake/cljc-validator
;
; @constant (map)
(def OPTIONS-TEST
     {:author           {:opt* true :f* map? :e* ":author must be a map!"
                         :name    {:opt* true :f* string? :e* ":name must be a string!"}
                         :website {:opt* true :f* string? :e* ":name must be a string!"}}
      :base-uri         {           :f* string? :not* empty? :e* ":base-uri must be a nonempty string!"}
      :filename-pattern {:opt* true :f* regex/pattern?       :e* ":filename-pattern must be a regex pattern!"}
      :library          {:opt* true :f* map?                 :e* ":library must be a map!"
                         :name    {:opt* true :f* string? :e* ":name must be a string!"}
                         :version {:opt* true :f* string? :e* ":version must be a string!"}
                         :website {:opt* true :f* string? :e* ":name must be a string!"}}
      :output-path      {           :f* string?              :not* empty? :e* ":output-path must be a nonempty string!"}
      :previews-path    {:opt* true :f* string?              :not* empty? :e* ":previews-path must be a nonempty string!"}
      :source-paths     {:and* [vector? #(every? string? %)] :not* empty? :e* ":source-paths must be a nonempty vector with string items!"}})
