import {style} from "@vanilla-extract/css"

export const neoChip = style({
  position: "relative",
  zIndex: 0,
  padding: "1rem 2rem",
  display: "flex",
  justifyContent: "center"
})

export const frame = style({
  "[data-name=bg]" : { color: "hsl(180, 75%, 10%)" },
  "[data-name=line]" : { color: "hsl(180, 75%, 50%)"},
})
