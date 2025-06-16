import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import Answer from "@/component/answer.jsx";
import axios from "@/util/axios.js";
import { MemoryRouter } from "react-router-dom";
import { vi } from "vitest";
import handleErrorLogout from "@/util/ErrorHandler.jsx";

// Mock hooks
vi.mock('react-auth-kit/hooks/useSignOut', () => ({
  default: () => vi.fn(),
}));
vi.mock('react-auth-kit/hooks/useAuthHeader', () => ({
  default: () => "Bearer testtoken",
}));
vi.mock('react-auth-kit/hooks/useAuthUser', () => ({
  default: () => ({ uid: "123" }),
}));
vi.mock('@/util/ErrorHandler.jsx', () => ({
  __esModule: true,
  default: vi.fn(),
}));

// Mock URL.createObjectURL for profile image
beforeAll(() => {
  global.URL.createObjectURL = vi.fn(() => "mocked-url");
});

afterAll(() => {
  global.URL.createObjectURL.mockRestore?.();
});

describe("Answer", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders answer with correct description, author, and profile image", async () => {
    // Mock axios.get for profile image
    vi.spyOn(axios, "get").mockResolvedValueOnce({
      status: 200,
      data: "Image Data"
    });

    render(
      <MemoryRouter>
        <Answer
          id="a1"
          description="This is an answer"
          createdAt="2024-01-01T00:00+00:00" // ISO 8601 UTC
          author={{ id: "123", username: "Alice" }}
          questionID="q1"
          onDelete={vi.fn()}
        />
      </MemoryRouter>
    );

    // Check for description
    const desc = screen.getByText("This is an answer");
    expect(desc.tagName.toLowerCase()).toBe("p");

    // Check for author.username
    const author = screen.getByText("Alice");
    expect(author.tagName.toLowerCase()).toMatch(/^h\d$/);

    // Check if the profile image is set
    const images = screen.queryAllByRole("img");
    images.forEach(image => {
       if (image.getAttribute("alt") === "User Avatar") {
          expect(image.getAttribute("src")).not.toBe("");
       }
     });

  });

  it("calls onDelete when delete button is clicked", async () => {
    // Mock axios.get for profile image
    vi.spyOn(axios, "get").mockResolvedValueOnce({
      status: 200,
      data: "Image Data"
    });
    // Mock axios.delete for answer deletion
    vi.spyOn(axios, "delete").mockResolvedValueOnce({
      status: 200
    });

    const onDelete = vi.fn();

    render(
      <MemoryRouter>
        <Answer
          id="a1"
          description="This is an answer"
          createdAt="2024-01-01"
          author={{ id: "123", username: "Alice" }}
          questionID="q1"
          onDelete={onDelete}
        />
      </MemoryRouter>
    );

    // Wait for the delete button to appear and click it
    const deleteBtn = await screen.findByTestId("deleteAnswerButton");
    fireEvent.click(deleteBtn);

    // Wait for onDelete to be called
    await waitFor(() => {
      expect(onDelete).toHaveBeenCalledWith("a1");
    });
  });

  it("calls handleErrorLogout if axios.get (icon) throws", async () => {
    vi.spyOn(axios, "get").mockRejectedValueOnce(new Error("Network Error"));

    render(
      <MemoryRouter>
        <Answer
          id="a1"
          description="This is an answer"
          createdAt="2024-01-01T00:00+00:00" // ISO 8601 UTC
          author={{ id: "123", username: "Alice" }}
          questionID="q1"
          onDelete={vi.fn()}
        />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(handleErrorLogout).toHaveBeenCalled();
    });
  });

  it("calls handleErrorLogout if axios.delete throws", async () => {
    // Mock axios.get for profile image
    vi.spyOn(axios, "get").mockResolvedValueOnce({
      status: 200,
      data: "Image Data"
    });
    // Mock axios.delete to throw
    vi.spyOn(axios, "delete").mockRejectedValueOnce(new Error("Delete Error"));

    render(
      <MemoryRouter>
        <Answer
          id="a1"
          description="This is an answer"
          createdAt="2024-05-29T00:00+00:00" // ISO 8601 UTC
          author={{ id: "123", username: "Alice" }}
          questionID="q1"
          onDelete={vi.fn()}
        />
      </MemoryRouter>
    );

    const deleteBtn = await screen.findByTestId("deleteAnswerButton");
    fireEvent.click(deleteBtn);

    await waitFor(() => {
      expect(handleErrorLogout).toHaveBeenCalled();
    });
  });

  it("calls handleErrorLogout if axios.delete returns status 400", async () => {
    // Mock axios.get for profile image
    vi.spyOn(axios, "get").mockResolvedValueOnce({
      status: 200,
      data: "Image Data"
    });
    // Mock axios.delete to return 400
    vi.spyOn(axios, "delete").mockResolvedValueOnce({
      status: 400
    });

    render(
      <MemoryRouter>
        <Answer
          id="a1"
          description="This is an answer"
          createdAt="2024-01-01T00:00+00:00" // ISO 8601 UTC
          author={{ id: "123", username: "Alice" }}
          questionID="q1"
          onDelete={vi.fn()}
        />
      </MemoryRouter>
    );

    const deleteBtn = await screen.findByTestId("deleteAnswerButton");
    fireEvent.click(deleteBtn);

    await waitFor(() => {
      expect(handleErrorLogout).toHaveBeenCalled();
    });
  });
});