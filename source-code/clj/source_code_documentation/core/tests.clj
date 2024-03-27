
(ns source-code-documentation.core.tests
    (:require [fruits.regex.api :as regex]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @links
; [cljc-validator](https://github.com/mt-utilities/cljc-validator)
;
; @constant (map)
(def OPTIONS-TEST
     {:author            {:name    {:opt* true :f* string? :e* ":name must be a string!"}
                          :website {:opt* true :f* string? :e* ":name must be a string!"}
                          :opt* true  :f* map?                             :e* ":author must be a map!"}
      :base-uri          {:opt* false :f* string? :not* empty?             :e* ":base-uri must be a nonempty string!"}
      :snippet-order     {:def      {:opt* true :and* [vector? #(every? keyword? %)] :e* ":snippet-order/def must be a vector with keyword items!"}
                          :defn     {:opt* true :and* [vector? #(every? keyword? %)] :e* ":snippet-order/defn must be a vector with keyword items!"}
                          :tutorial {:opt* true :and* [vector? #(every? keyword? %)] :e* ":snippet-order/tutorial must be a vector with keyword items!"}
                          :opt* true}
      :favicon-uri       {:opt* true  :f* string?                          :e* ":favicon must be a string!"}
      :filename-pattern  {:opt* true  :f* regex/pattern?                   :e* ":filename-pattern must be a regex pattern!"}
      :library           {:opt* true  :f* map?                             :e* ":library must be a map!"
                          :name    {:opt* true :f* string? :e* ":name must be a string!"}
                          :version {:opt* true :f* string? :e* ":version must be a string!"}
                          :website {:opt* true :f* string? :e* ":name must be a string!"}}
      :output-path       {           :f* string?              :not* empty? :e* ":output-path must be a nonempty string!"}
      :previews-uri      {:opt* true :f* string?              :not* empty? :e* ":previews-uri must be a nonempty string!"}
      :source-paths      {:and* [vector? #(every? string? %)] :not* empty? :e* ":source-paths must be a nonempty vector with string items!"}})
