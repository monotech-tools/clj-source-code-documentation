
(ns source-code-documentation.print.styles)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (string)
(def STYLES "

/*** Body ***/

body {
  font-family: Montserrat;
  font-size:   14px }

/*** Body ***/

/*** Labels, links ***/

.list-block-title {
  color:       #101010;
  display:     block;
  font-size:   14px;
  line-height: 36px;
  padding:     0 12px;
  width:       100% }

.list-block-helper {
  color:       #a0a0a0;
  display:     block;
  font-size:   12px;
  line-height: 24px;
  padding:     0 12px;
  width:       100% }

.list-block-link,
.list-block-link:active,
.list-block-link:hover,
.list-block-link:visited,
#library-website,
#library-website:active,
#library-website:hover,
#library-website:visited {
  color:           #0088cc;
  display:         block;
  font-size:       14px;
  line-height:     24px;
  padding:         0 12px;
  text-decoration: none }

.list-block-link:hover {
  background-color: #f0f0f0 }

#library-name {
  font-weight:    600;
  line-height:    24px;
  text-transform: uppercase
}

/*** Labels, links ***/

/*** Bars ***/

#top-bar {
  background-color: #ffffff;
  border-bottom:    1px solid #e0e0e0;
  height:           60px;
  left:             0;
  position:         fixed;
  top:              0;
  width:            100% }

#library-website {
  position: absolute;
  right:    18px;
  top:      18px }

#library-name {
  left:     18px;
  position: absolute;
  top:      18px }

/*** Bars ***/

/*** Lists ***/

#namespace-list {
  border-right:   1px solid #e0e0e0;
  display:        flex;
  flex-direction: column;
 -gap:            12px;
  height:         calc(100vh - 60px);
  left:           0;
  position:       fixed;
  top:            60px;
  width:          240px }

#symbol-list {
  border-right:   1px solid #e0e0e0;
  display:        flex;
  flex-direction: column;
 -gap:            12px;
  height:         calc(100vh - 60px);
  left:           240px;
  position:       fixed;
  top:            60px;
  width:          240px }

.sidelist-scroll-container {
  flex-grow:  1;
  overflow-y: auto;
}

#header-list {
  padding-left: 528px;
  padding-top:  60px;
}

.header-list--header {
  padding-top: 60px;
}

/*** Lists ***/
")
