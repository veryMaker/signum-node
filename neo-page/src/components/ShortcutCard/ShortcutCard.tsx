import * as classes from "./ShortcutCard.css";

type Link = {
  title: string;
  description: string;
  href: string;
  cta: string;
};

interface Props {
  side: "left" | "right";
  imgSrc: string;
  title: string;
  links: Link[];
}

export const ShortcutCard = ({ side, imgSrc, title, links }: Props) => {
  return (
    <div className={classes.card({ variant: side })}>
      <picture className={classes.pictureContainer}>
        <img src={imgSrc} alt={title} className={classes.img} />
      </picture>

      <div className={classes.contentContainer}>
        <h4 className={classes.contentTitle}>{title}</h4>

        {links.map((link, index) => (
          <div className={classes.contentColumn} key={index}>
            <h6 className={classes.columnTitle}>{link.title}</h6>
            <span className={classes.columnDescription}>
              {link.description}
            </span>
            <a href={link.href} target="_blank">
              <b>{`${link.cta} >`}</b>
            </a>
          </div>
        ))}
      </div>
    </div>
  );
};
