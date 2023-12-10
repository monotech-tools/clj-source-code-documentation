
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
.list-block-link:visited {
  color:           #0088cc;
  display:         block;
  font-size:       14px;
  line-height:     24px;
  padding:         0 12px;
  text-decoration: none }

.list-block-link:hover {
  background-color: #f0f0f0 }

/*** Labels, links ***/

/*** Lists ***/

#namespace-list {
  border-right:   1px solid #e0e0e0;
  display:        flex;
  flex-direction: column;
 -gap:            12px;
  height:         100vh;
  left:           0;
  position:       fixed;
  top:            0;
  width:          240px }

#symbol-list {
  border-right:   1px solid #e0e0e0;
  display:        flex;
  flex-direction: column;
 -gap:            12px;
  height:         100vh;
  left:           240px;
  position:       fixed;
  top:            0;
  width:          240px }

.sidelist-scroll-container {
  flex-grow:  1;
  overflow-y: auto;
}

#header-list {
  padding-left: 480px;
}

/*** Lists ***/
")
