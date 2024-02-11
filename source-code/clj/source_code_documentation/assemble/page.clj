
(ns source-code-documentation.assemble.page
    (:require [asset-compressor.api                     :as asset-compressor]
              [fruits.hiccup.api                        :as hiccup]
              [fruits.random.api                        :as random]
              [fruits.string.api                        :as string]
              [fruits.uri.api                           :as uri]
              [fruits.vector.api                        :as vector]
              [hiccup.page                              :refer [html5]]
              [io.api                                   :as io]
              [source-code-documentation.assemble.css   :as assemble.css]
              [source-code-documentation.assemble.js    :as assemble.js]
              [source-code-documentation.assemble.utils :as assemble.utils]
              [source-code-documentation.assemble.prototypes :as assemble.prototypes]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-content-block-header
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ; @param (keyword) block-id
  ; @param (map) block-props
  ; {:collapsible? (boolean)(opt)
  ;  :meta-class (keyword or keywords in vector)(opt)
  ;  :name-class (keyword or keywords in vector)(opt)
  ;  :type-class (keyword or keywords in vector)(opt)
  ;  ...}
  ;
  ; @return (hiccup)
  [_ _ content-block block-id {:keys [collapsible? meta-class name-class type-class]}]
  (let [toggle-f (str "toggleCollapsible('"block-id"')")]
       [(if collapsible? :button :div)
        (if collapsible? {:class [:content-block--header] :onClick toggle-f}
                         {:class [:content-block--header]})
        (if-let [type (-> content-block :type)]        [:pre {:class type-class} (-> type)])
        (if-let [meta (-> content-block :meta first)]  [:pre {:class meta-class} (-> meta)])
        (if-let [meta (-> content-block :meta second)] [:pre {:class meta-class} (-> meta)])
        (if-let [name (-> content-block :name)]        [:pre {:class name-class} (-> name)])]))

(defn assemble-content-block-text
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ; @param (keyword) block-id
  ; @param (map) block-props
  ; {:text-class (keyword or keywords in vector)(opt)
  ;  ...}
  ;
  ; @return (hiccup)
  [_ _ content-block _ {:keys [text-class]}]
  (if (-> content-block :text vector/not-empty?)
      [:div {:class [:content-block--text]}
            (vector/concat-items [:pre {:class text-class}]
                                 (-> content-block :text assemble.utils/gap-rows assemble.utils/parse-links assemble.utils/unparse-html))]))

(defn assemble-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ; @param (map) block-props
  ; {:collapsed? (boolean)(opt)
  ;  :collapsible? (boolean)(opt)
  ;  :meta-class (keyword or keywords in vector)(opt)
  ;  :name-class (keyword or keywords in vector)(opt)
  ;  :text-class (keyword or keywords in vector)(opt)
  ;  :type-class (keyword or keywords in vector)(opt)}
  ;
  ; @return (hiccup)
  [state options content-block block-props]
  (let [block-id         (random/generate-string)
        block-props      (assemble.prototypes/block-props-prototype content-block block-id block-props)
        data-collapsed   (-> block-props :collapsed?   boolean str)
        data-collapsible (-> block-props :collapsible? boolean str)]
       [:div {:class [:content-block] :id block-id :data-collapsed data-collapsed :data-collapsible data-collapsible}
             (assemble-content-block-header state options content-block block-id block-props)
             (assemble-content-block-text   state options content-block block-id block-props)]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-atom-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  (assemble-content-block state options content-block
                          {:collapsed?   true
                           :collapsible? true
                           :meta-class [:color--soft-grey :text--xs]
                           :name-class [:color--hard-grey :text--xs :text--bold]
                           :text-class [:color--hard-grey :text--s  :text--boxed :scroll-x]
                           :type-class [:color--soft-grey :text--xs]}))

(defn assemble-bug-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  (assemble-content-block state options content-block
                          {:collapsed?   true
                           :collapsible? true
                           :meta-class [:color--soft-grey :text--xs]
                           :name-class [:color--hard-grey :text--xs :text--bold]
                           :text-class [:color--hard-grey :text--s  :text--boxed :text--wrap]
                           :type-class [:color--hard-red  :text--xs]}))

(defn assemble-code-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  (assemble-content-block state options content-block
                          {:meta-class [:color--soft-grey :text--xs]
                           :name-class [:color--hard-grey :text--xs :text--bold]
                           :text-class [:color--hard-grey :text--s  :text--boxed :scroll-x]
                           :type-class [:color--soft-grey :text--xs]}))

(defn assemble-constant-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  (assemble-content-block state options content-block
                          {:collapsed?   true
                           :collapsible? true
                           :meta-class [:color--soft-grey :text--xs]
                           :name-class [:color--hard-grey :text--xs :text--bold]
                           :text-class [:color--hard-grey :text--s  :text--boxed :scroll-x]
                           :type-class [:color--soft-grey :text--xs]}))

(defn assemble-description-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  (assemble-content-block state options content-block
                          {:meta-class [:color--soft-grey :text--xs]
                           :name-class [:color--hard-grey :text--xs :text--bold]
                           :text-class [:color--hard-grey :text--s  :text--boxed :text--wrap]
                           :type-class [:color--soft-grey :text--xs]}))

(defn assemble-error-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  (assemble-content-block state options content-block
                          {:meta-class [:color--soft-grey :text--xs]
                           :name-class [:color--hard-grey :text--xs :text--bold]
                           :text-class [:color--hard-red  :text--s  :text--boxed :scroll-y]
                           :type-class [:color--hard-red  :text--xs]}))

(defn assemble-example-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  (assemble-content-block state options content-block
                          {:meta-class [:color--soft-grey :text--xs]
                           :name-class [:color--hard-grey :text--xs :text--bold]
                           :text-class [:color--hard-grey :text--s  :text--boxed :scroll-x]
                           :type-class [:color--soft-grey :text--xs]}))

(defn assemble-important-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  (assemble-content-block state options content-block
                          {:meta-class [:color--soft-grey :text--xs]
                           :name-class [:color--hard-grey :text--xs :text--bold]
                           :text-class [:color--hard-grey :text--s  :text--boxed :text--wrap]
                           :type-class [:color--hard-red  :text--xs]}))

(defn assemble-info-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  (assemble-content-block state options content-block
                          {:meta-class [:color--soft-grey :text--xs]
                           :name-class [:color--hard-grey :text--xs :text--bold]
                           :text-class [:color--hard-grey :text--s  :text--boxed :text--wrap]
                           :type-class [:color--hard-blue :text--xs]}))

(defn assemble-note-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  (assemble-content-block state options content-block
                          {:meta-class [:color--soft-grey :text--xs]
                           :name-class [:color--hard-grey :text--xs :text--bold]
                           :text-class [:color--hard-grey :text--s  :text--boxed :text--wrap]
                           :type-class [:color--hard-blue :text--xs]}))

(defn assemble-param-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  ; (-> content-block :meta second (case "opt" "optional" "req" "required" "required"))
  (assemble-content-block state options content-block
                          {:collapsed?   true
                           :collapsible? true
                           :meta-class [:color--soft-grey :text--xs]
                           :name-class [:color--hard-grey :text--xs :text--bold]
                           :text-class [:color--hard-grey :text--s  :text--boxed :scroll-x]
                           :type-class [:color--soft-grey :text--xs]}))

(defn assemble-plain-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  (assemble-content-block state options content-block
                          {:meta-class [:color--soft-grey :text--xs]
                           :name-class [:color--hard-grey :text--xs :text--bold]
                           :text-class [:color--hard-grey :text--s  :text--boxed :text--wrap]
                           :type-class [:text--hidden]}))

(defn assemble-preview-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  (let [preview-image-uri (assemble.utils/preview-image-uri state options content-block)]
       [:img {:class [:content-block--preview-image] :src preview-image-uri}]))
       ; TODO

(defn assemble-return-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  (assemble-content-block state options content-block
                          {:collapsed?   true
                           :collapsible? true
                           :meta-class [:color--soft-grey :text--xs]
                           :name-class [:color--hard-grey :text--xs :text--bold]
                           :text-class [:color--hard-grey :text--s  :text--boxed :scroll-x]
                           :type-class [:color--soft-grey :text--xs]}))

(defn assemble-source-code-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  (assemble-content-block state options content-block
                          {:collapsed?   true
                           :collapsible? true
                           :meta-class [:color--soft-grey :text--xs]
                           :name-class [:color--hard-grey :text--xs :text--bold]
                           :text-class [:color--hard-grey :text--s  :text--boxed :scroll-x]
                           :type-class [:color--hard-grey :text--xs]}))

(defn assemble-title-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  (assemble-content-block state options content-block
                          {:meta-class [:color--soft-grey :text--xs]
                           :name-class [:color--hard-blue :text--l :text--bold]
                           :text-class [:color--hard-grey :text--s :text--boxed :text--wrap]
                           :type-class [:text--hidden]}))

(defn assemble-todo-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  (assemble-content-block state options content-block
                          {:collapsed?   true
                           :collapsible? true
                           :meta-class [:color--soft-grey   :text--xs]
                           :name-class [:color--hard-grey   :text--xs :text--bold]
                           :text-class [:color--hard-grey   :text--s  :text--boxed :text--wrap]
                           :type-class [:color--hard-purple :text--xs]}))

(defn assemble-unknown-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  (assemble-content-block state options content-block
                          {:meta-class [:color--soft-grey :text--xs]
                           :name-class [:color--hard-grey :text--xs :text--bold]
                           :text-class [:color--hard-grey :text--s  :text--boxed :scroll-x]
                           :type-class [:color--soft-grey :text--xs]}))

(defn assemble-usage-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  (assemble-content-block state options content-block
                          {:meta-class [:color--soft-grey :text--xs]
                           :name-class [:color--hard-grey :text--xs :text--bold]
                           :text-class [:color--hard-grey :text--s  :text--boxed :scroll-x]
                           :type-class [:color--soft-grey :text--xs]}))



; Deprecated!
; Tutorial blocks are not supported within declaration headers!
(defn assemble-tutorial-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [_ _ _])
; Deprecated!

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-content-block-selector
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  (case (-> content-block :type)
        :atom        (assemble-atom-content-block        state options content-block)
        :bug         (assemble-bug-content-block         state options content-block)
        :code        (assemble-code-content-block        state options content-block)
        :constant    (assemble-constant-content-block    state options content-block)
        :description (assemble-description-content-block state options content-block)
        :error       (assemble-error-content-block       state options content-block)
        :example     (assemble-example-content-block     state options content-block)
        :important   (assemble-important-content-block   state options content-block)
        :info        (assemble-info-content-block        state options content-block)
        :note        (assemble-note-content-block        state options content-block)
        :param       (assemble-param-content-block       state options content-block)
        :plain       (assemble-plain-content-block       state options content-block)
        :preview     (assemble-preview-content-block     state options content-block)
        :return      (assemble-return-content-block      state options content-block)
        :source-code (assemble-source-code-content-block state options content-block)
        :title       (assemble-title-content-block       state options content-block)
        :todo        (assemble-todo-content-block        state options content-block)
        :usage       (assemble-usage-content-block       state options content-block)
                     (assemble-unknown-content-block     state options content-block)))
       ; Deprecated!
       ; Tutorials are not content blocks, they are imported as sections, and there is no such thing as tutorial content block.
       ;:tutorial    (assemble-tutorial-content-block    state options content-block)
       ; Deprecated!

(defn assemble-content-blocks
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) section
  ; {:content (maps in vector)
  ;  ...}
  ;
  ; @return (hiccup)
  [state options {:keys [content]}]
  (letfn [(f0 [%] (assemble-content-block-selector state options %))]
         (hiccup/put-with [:div {:class [:content-blocks]}] content f0)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-section-header
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) section
  ; @param (map) header-props
  ; {:label-class (keyword or keywords in vector)(opt)
  ;  ...}
  ;
  ; @return (hiccup)
  [_ _ section {:keys [label-class]}]
  [:div {:class [:section--header]}
        [:pre {:class [:color--soft-grey :text--xs]} (:type  section)]
        [:pre {:class label-class}                   (:label section)]])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-declaration
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) section
  ;
  ; @return (hiccup)
  [state options section]
  (let [section        (->> section (assemble.utils/append-declaration-source-code state options))
        section        (->> section (assemble.utils/sort-declaration-content       state options))
        declaration-id (->> section :name hiccup/value)]
       [:div {:id declaration-id :class [:section]}
             (assemble-section-header state options section {:label-class [:color--hard-blue :text--xl :text--bold]})
             (assemble-content-blocks state options section)]))

(defn assemble-declarations
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  ; @note (#3901)
  ; (vector/sort-items-by [...] :name)
  (letfn [(f0 [%] (-> state (assemble-declaration options %)))
          (f1 [%] (-> state (assemble.utils/filter-sections options file-data %)))]
         (-> [:div {:class [:sections]}]
             (hiccup/put-with (f1 :def)  f0)
             (hiccup/put-with (f1 :defn) f0))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-tutorial
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) section
  ;
  ; @return (hiccup)
  [state options section]
  (let [section     (->> section (assemble.utils/sort-tutorial-content state options))
        tutorial-id (->> section :name hiccup/value)]
       [:div {:id tutorial-id :class [:section]}
             (assemble-section-header state options section {:label-class [:color--hard-purple :text--xl :text--wrap :text--bold]})
             (assemble-content-blocks state options section)]))

(defn assemble-tutorials
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  ; @note (#3901)
  ; (vector/sort-items-by [...] :name)
  (letfn [(f0 [%] (-> state (assemble-tutorial options %)))
          (f1 [%] (-> state (assemble.utils/filter-sections options file-data %)))]
         (-> [:div {:class [:sections]}]
             (hiccup/put-with (f1 :tutorial) f0))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-namespace-header
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [_ _ file-data]
  [:div {:id :namespace-header}
        [:pre {:id :namespace-header--title :class [:text--xl :text--bold]}
              (-> file-data :ns-map :declaration :name)]
        [:pre {:class [:color--soft-grey :text--xs]}
              (-> file-data :filepath io/filepath->extension
                  (case "clj" "Clojure namespace" "cljc" "Isomorphic namespace" "cljs" "ClojureScript namespace" "Unknown"))]])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-tutorial-list
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  ; @note (#3901)
  (letfn [(f0 [%] (-> state (assemble.utils/symbol-anchor   options file-data %)))
          (f1 [%] (-> state (assemble.utils/filter-sections options file-data %)))
          (f2 [%] [:a {:href (f0 %)} [:pre {:class [:button :color--hard-purple :text--m]} (-> % :label)]])]
         (if-let [tutorials (f1 :tutorial)]
                 (-> [:div {:class [:secondary-list--container]} [:pre {:class [:color--soft-grey :text--xs]} "Tutorials"]]
                     (hiccup/put-with tutorials f2)))))

(defn assemble-declaration-list
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  ; @note (#3901)
  (letfn [(f0 [%] (-> state (assemble.utils/symbol-anchor   options file-data %)))
          (f1 [%] (-> state (assemble.utils/filter-sections options file-data %)))
          (f2 [%] (-> %     (vector/sort-items-by :name)))
          (f3 [%] [:a {:href (f0 %)} [:pre {:class [:button :color--hard-blue :text--m]} (-> % :name)]])]
         (let [defs (f1 :def) defns (f1 :defn)]
              (if (or defs defns)
                  (-> [:div {:class [:secondary-list--container]} [:pre {:class [:text--xs :color--soft-grey]} "Declarations"]]
                      (hiccup/put-with (f2 defs)  f3)
                      (hiccup/put-with (f2 defns) f3))))))

(defn assemble-secondary-list
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  ; @note (#3901)
  [:div {:id :secondary-list}
        [:div {:class [:scroll-y]}
              (assemble-tutorial-list    state options file-data)
              (assemble-declaration-list state options file-data)]])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-namespace-list
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (string) extension
  ;
  ; @return (hiccup)
  [state options file-data extension]
  ; @note (#3901)
  (letfn [(f0 [%] (-> state (assemble.utils/namespace-uri     options % extension)))
          (f1 [%] (-> state (assemble.utils/filter-namespaces options %)))
          (f2 [%] (-> %     (assemble.utils/sort-namespaces   options)))
          (f3 [%] (and (-> % :filepath io/filepath->extension (= extension)) (= file-data %)))
          (f4 [%] [:a {:href (f0 %)} [:pre {:class [:button :color--hard-blue :text--m (if (f3 %) :button--active)]} (-> % :ns-map :declaration :name)]])]
         (if-let [namespaces (f1 extension)]
                 (let [label (case extension "clj" "Clojure namespaces" "cljc" "Isomorphic namespaces" "cljs" "ClojureScript namespaces")]
                      (-> [:div {:class [:primary-list--container]} [:pre {:class [:color--soft-grey :text--xs]} label]]
                          (hiccup/put-with (f2 namespaces) f4))))))

(defn assemble-primary-list
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  ; @note (#3901)
  ; - The namespace list and declaration list items are alphabetically ordered (to make searching items easy).
  ; - Tutorial list items are displayed in their written order as they were placed in files (their written order could be important!).
  ; - The actual declaration and tutorial sections are displayed in their written order as they were placed in files (their written order could be important!).
  [:div {:id :primary-list}
        [:div {:class [:scroll-y]}
              (assemble-namespace-list state options file-data "clj")
              (assemble-namespace-list state options file-data "cljc")
              (assemble-namespace-list state options file-data "cljs")]])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-top-bar
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [_ options _]
  [:div {:id :top-bar}
        [:pre {:id :top-bar--library-name    :class [:text--xl :text--bold]}      (-> options :library :name)]
        [:pre {:id :top-bar--library-version :class [:color--soft-grey :text--s]} (-> options :library :version)]
        (let [library-website-pretty-url (-> options :library :website uri/pretty-url)
              library-website-valid-url  (-> options :library :website uri/valid-url)]
             [:a {:id :top-bar--library-uri :class [:text--s] :href library-website-valid-url}
                 [:pre {:class [:color--hard-blue]} (-> library-website-pretty-url)]])])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-bottom-bar
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [_ _ _]
  [:div {:id :bottom-bar}
        [:a {:id :bottom-bar--credits-link :class [:color--soft-purple :text--s] :href "https://github.com/mt-devtools/clj-source-code-documentation"}
            [:pre "github.com/mt-devtools/clj-source-code-documentation"]]])

(defn assemble-page-body
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  [:body (assemble-primary-list     state options file-data)
         (assemble-secondary-list   state options file-data)
         (assemble-namespace-header state options file-data)
         (assemble-tutorials        state options file-data)
         (assemble-declarations     state options file-data)
         (assemble-top-bar          state options file-data)
         (assemble-bottom-bar       state options file-data)])

(defn assemble-page-head
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [_ _ _]
  [:head [:style  {:type "text/css"}        (asset-compressor/compress-css assemble.css/STYLES)]
         [:script {:type "text/javascript"} (asset-compressor/compress-js  assemble.js/SCRIPTS)]])

(defn assemble-page
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (string)
  [state options file-data]
  (html5 {} [:html (-> state (assemble-page-head options file-data))
                   (-> state (assemble-page-body options file-data) hiccup/unparse-class-vectors hiccup/unparse-css)]))
