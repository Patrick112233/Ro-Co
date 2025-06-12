import React from "react";
import { render, screen } from "@testing-library/react";
import Popup from "@/component/popup.jsx";

describe("Popup", () => {
  it("does not render when trigger is false", () => {
    render(
      <Popup trigger={false}>
        <div>Popup Content</div>
      </Popup>
    );
    // Should not find the content
    expect(screen.queryByText("Popup Content")).toBeNull();
  });

  it("renders children when trigger is true", () => {
    render(
      <Popup trigger={true}>
        <div>Popup Content</div>
      </Popup>
    );
    // Should find the content
    expect(screen.getByText("Popup Content")).toBeInTheDocument();
  });
});