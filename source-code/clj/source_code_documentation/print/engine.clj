
(ns source-code-documentation.print.engine
    (:require [hiccup.page :refer [html5]]
              [fruits.hiccup.api :as hiccup]
              [io.api :as io]
              [fruits.vector.api :as vector]
              [fruits.string.api :as string]
              [fruits.map.api :as map]
              [source-code-documentation.print.styles :as print.styles]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn filter-namespaces
  [state _ extension]
  (letfn [(f0 [%] (-> % :filepath io/filepath->extension (= extension)))]
         (-> state (vector/keep-items-by f0)
                   (vector/to-nil {:if-empty? true}))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-header-description-block
  [state options file-data header header-block]
  [:div {:style {:display :flex :gap "4px"}}
        [:span {:style {:color "#888" :font-size :10px :line-height :18px :text-transform :uppercase}}
               "Description"]
        (vector/concat-items [:pre {:style {:font-size :12px :line-height :16px :background "#fafafa" :padding "6px" :color "#333" :margin 0}}]
                             (-> header-block :additional (vector/gap-items [:br])))])

(defn assemble-header-return-block
  [state options file-data header header-block]
  [:div {:style {:display :flex :gap "4px"}}
        [:span {:style {:color "#888" :font-size :10px :line-height :18px :text-transform :uppercase}}
               "Return"]
        [:span {:style {:line-height :18px :color "#888" :text-transform :uppercase :font-size :10px}}
               (-> header-block :meta first)]])

(defn assemble-header-example-block
  [state options file-data header header-block]
  [:div {:style {}}
        [:span {:style {:color "#888" :font-size :10px :line-height :18px :text-transform :uppercase}}
               "Example"]
        (vector/concat-items [:pre {:style {:font-size :12px :line-height :16px :background "#fafafa" :padding "6px" :color "#333" :margin 0}}]
                             (vector/gap-items (:additional header-block) [:br]))])

(defn assemble-header-usage-block
  [state options file-data header header-block]
  [:div {:style {}}
        [:span {:style {:color "#888" :font-size :10px :line-height :18px :text-transform :uppercase}}
               "Usage"]
        (vector/concat-items [:pre {:style {:font-size :12px :line-height :16px :background "#fafafa" :padding "6px" :color "#333" :margin 0}}]
                             (vector/gap-items (:additional header-block) [:br]))])

(defn assemble-header-param-block
  [state options file-data header header-block]
  [:div {:style {:display :flex :gap "4px"}}
        [:span {:style {:color "#888" :font-size :10px :line-height :18px :text-transform :uppercase}}
               "Param"]
        [:span {:style {:line-height :18px}}
               (:value header-block)]
        [:span {:style {:line-height :18px :color "#888" :text-transform :uppercase :font-size :10px}}
               (-> header-block :meta first)]
        [:span {:style {:line-height :18px :color "#888" :text-transform :uppercase :font-size :10px}}
               (case (-> header-block :meta second) "opt" "optional"
                                                    "req" "required"
                                                          "required")]])
        ;header-block])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-header-block
  [state options file-data header header-block]
  (case (-> header-block :type)
        :param   (assemble-header-param-block   state options file-data header header-block)
        :example (assemble-header-example-block state options file-data header header-block)
        :return  (assemble-header-return-block  state options file-data header header-block)
        :usage   (assemble-header-usage-block   state options file-data header header-block)
        :description (assemble-header-description-block state options file-data header header-block)
                 [:div (str header-block)]))

(defn assemble-header
  [state options file-data header]
  [:div {:id (-> header :name hiccup/value)}
         ;:style {:display :flex :flex-direction :column :gap :12px}}
        [:span {:style {:font-weight 600}}
         (:name header)]
        (letfn [(f0 [%] (assemble-header-block state options file-data header %))]
               (hiccup/put-with [:div] (-> header :blocks) f0))])

(defn assemble-header-list
  [state options file-data]
  [:div {:id :header-list}
        (letfn [(f0 [%] (assemble-header state options file-data %))]
               (hiccup/put-with [:div {:style {:display :flex :flex-direction :column :gap :48px}}]
                                (-> file-data :headers) f0))])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-symbol-list
  [state options file-data]
  ; use anchors #my-function
  [:div {:id :symbol-list}
        [:span {:class :list-block-title}
               (-> file-data :ns-map :declaration :name)]
        [:span {:class :list-block-helper}
               (case (-> file-data :filepath io/filepath->extension)
                     "clj"  "Clojure namespace"
                     "cljc" "Isomorphic namespace"
                     "cljs" "ClojureScript namespace")]
        (hiccup/put-with [:div {:class :sidelist-scroll-container}]
                         (-> file-data :headers (vector/->items :name) vector/abc-items)
                         (fn [%] [:a {:class :list-block-link :href (str "#" (hiccup/value %))} %]))])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-namespace-list
  [state options]
  (letfn [(f0 [%] (-> % :ns-map :declaration :name))
          (f1 [%] (str "/" (string/replace-part % "." "/") ".html"))
          (f2 [%] [:a {:class :list-block-link :href (f1 %)} %])
          (f3 [%] (-> % (vector/->items f0) vector/abc-items))]
         [:div {:id :namespace-list}
               [:div {:class :sidelist-scroll-container}
                     (if-let [clj-namespace-list (filter-namespaces state options "clj")]
                             (hiccup/put-with [:div [:span {:class :list-block-helper} "Clojure namespaces"]] (-> clj-namespace-list f3) f2))
                     (if-let [cljc-namespace-list (filter-namespaces state options "cljc")]
                             (hiccup/put-with [:div [:span {:class :list-block-helper} "Isomorphic namespaces"]] (-> cljc-namespace-list f3) f2))
                     (if-let [cljs-namespace-list (filter-namespaces state options "cljs")]
                             (hiccup/put-with [:div [:span {:class :list-block-helper} "ClojureScript namespaces"]] (-> cljs-namespace-list f3) f2))]]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-body
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (hiccup)
  [state options]
  [:body (assemble-namespace-list state options)
         (assemble-symbol-list    state options (second state))
         (assemble-header-list    state options (second state))])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-head
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (hiccup)
  [_ _]
  [:head [:link {:rel "stylesheet" :href "https://fonts.googleapis.com/css2?family=Montserrat:wght@100;200;300;400;500;600;700;800;900&display=swap"}]
         [:style print.styles/STYLES]])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn print-documentation!
  [state options]
  (let [state (vector/keep-items-by state :create-documentation?)]
       (html5 {} [:html                     (assemble-head state options)
                        (hiccup/unparse-css (assemble-body state options))])))
