import { Fragment, useEffect } from "react";
import { Title } from "./sections/Title";
import { Videotutorials } from "./sections/Videotutorials";
import { Wikis } from "./sections/Wikis";
import { Developer } from "./sections/Developer";

export function DocsPage() {
  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  return (
    <Fragment>
      <Title />
      <Videotutorials />
      <Wikis />
      <Developer />
    </Fragment>
  );
}
