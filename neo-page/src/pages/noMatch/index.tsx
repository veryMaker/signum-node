import { Link } from "react-router-dom";

export function NoMatchPage() {
  return (
    <div
      style={{
        width: "100%",
        height: "75vh",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        flexDirection: "column",
        overflow: "hidden",
      }}
    >
      <h2>Nothing to see here!</h2>
      <p>
        <Link to="/">Go to the home page</Link>
      </p>
    </div>
  );
}
