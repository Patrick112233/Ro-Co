import React from "react";
import { render, screen, fireEvent, waitFor, cleanup } from "@testing-library/react";
import Question from "@/component/question.jsx";
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
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => vi.fn(),
  };
});

// Mock URL.createObjectURL for profile image
beforeAll(() => {
  global.URL.createObjectURL = vi.fn(() => "mocked-url");
});
afterAll(() => {
  global.URL.createObjectURL.mockRestore?.();
});

describe("Question", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders correctly without answers, fetchAnswers not called, and profile image is loaded", async () => {
    // Mock axios.get for profile image
    const getSpy = vi.spyOn(axios, "get").mockResolvedValueOnce({
      status: 200,
      data: "Image Data"
    });

    render(
      <MemoryRouter>
        <Question
          id="q1"
          title="Test Title"
          description="Test Description"
          createdAt="2024-01-01T00:00+00:00" // ISO 8601 UTC
          answered={false}
          author={{ id: "123", username: "Alice" }}
          onDelete={vi.fn()}
        />
      </MemoryRouter>
    );

    // Title and description rendered
    expect(screen.getByText("Test Title")).toBeInTheDocument();
    expect(screen.getByText("Test Description")).toBeInTheDocument();

    // isOpen section should not be rendered
    expect(screen.queryByTestId("comment")).toBeNull();

    // Profile image loaded
    const images = screen.queryAllByRole("img");
    images.forEach(image => {
       if (image.getAttribute("alt") === "User Avatar") {
          expect(image.getAttribute("src")).not.toBe("");
       }
     });

    // fetchAnswers should not be called
    expect(getSpy).toHaveBeenCalledTimes(1);
  });


  it("answered and delete btns only visible for author, and answered btn color", async () => {
    // Mock axios.get for profile image
    vi.spyOn(axios, "get").mockResolvedValueOnce({
      status: 200,
      data: "Image Data"
    });

    // Author is current user
    render(
      <MemoryRouter>
        <Question
          id="q1"
          title="Test Title"
          description="Test Description"
          createdAt="2024-01-01T00:00+00:00" // ISO 8601 UTC
          answered={true}
          author={{ id: "123", username: "Alice" }}
          onDelete={vi.fn()}
        />
      </MemoryRouter>
    );

    // Answered and delete btn should be visible 
    const answeredBtn = screen.getByTestId("DeleteQuestionButton");
    const checkIcon = screen.getByTestId("ToggleAnsweredButton");
    expect(checkIcon).toBeInTheDocument();
    expect(answeredBtn).toBeInTheDocument();

    cleanup(); // <-- clear the DOM

    // Now render as non-author
    render(
      <MemoryRouter>
        <Question
          id="q2"
          title="Other Title"
          description="Other Description"
          createdAt="2024-01-01T00:00+00:00" // ISO 8601 UTC
          answered={false}
          author={{ id: "999", username: "Bob" }}
          onDelete={vi.fn()}
        />
      </MemoryRouter>
    );
    // Answered btn should be invisible (since not answered and user is not author)
    expect(screen.queryByTestId("ToggleAnsweredButton")).toBeNull();
    // Delete btn should not be visible (since not author)
    expect(screen.queryByTestId("deleteQuestionButton")).toBeNull();
  });


  it("fetches paginated answers and renders Answer components when open", async () => {
    // Mock paginated axios.get for answers (2 pages: 10 + 11)
    const answersPage1 = Array.from({ length: 10 }, (_, i) => ({
      id: `a${i + 1}`,
      description: `Answer ${i + 1}`,
      createdAt: `2024-01-01T00:00+00:00`, // ISO 8601 UTC,
      author: { id: "u1", username: `User${i + 1}` }
    }));
    const answersPage2 = Array.from({ length: 11 }, (_, i) => ({
      id: `a${i + 11}`,
      description: `Answer ${i + 11}`,
      createdAt: `2024-01-01T00:00+00:00`, // ISO 8601 UTC,
      author: { id: "u2", username: `User${i + 11}` }
    }));

    // First call: profile image, then two pages of answers
    vi.spyOn(axios, "get")
      .mockResolvedValueOnce({ status: 200, data: "Image Data" }) // profile image
      .mockResolvedValueOnce({ status: 200, data: answersPage1 }) // page 1
      .mockResolvedValueOnce({ status: 200, data: answersPage2 }) // page 2
      .mockResolvedValueOnce({ status: 200, data: [] }); // end

    render(
      <MemoryRouter>
        <Question
          id="q1"
          title="Test Title"
          description="Test Description"
          createdAt="2024-01-01T00:00+00:00" // ISO 8601 UTC
          answered={false}
          author={{ id: "123", username: "Alice" }}
          onDelete={vi.fn()}
        />
      </MemoryRouter>
    );

    // Open answers section
    fireEvent.click(screen.getByTestId("OpenAnswerButton"));

    // Wait for a answers to appear
    await waitFor(() => {
      expect(screen.getByText("Answer 1")).toBeInTheDocument();
      expect(screen.getByText("Answer 15")).toBeInTheDocument();
      expect(screen.getByText("Answer 21")).toBeInTheDocument();
    });
  });

  it("posts a new answer and renders it", async () => {
    // Keep order of axios.get calls:
    vi.spyOn(axios, "get").mockResolvedValueOnce({ status: 200, data: "Image Data" });
    // Answers fetch (empty)
    vi.spyOn(axios, "get").mockResolvedValueOnce({ status: 200, data: [] });

    // Mock post
    vi.spyOn(axios, "post").mockResolvedValueOnce({
      status: 201,
      data: {
        id: "a1",
        description: "My new answer",
        createdAt: "2024-01-01T00:00+00:00", // ISO 8601 UTC
        author: { id: "123", username: "Alice" }
      }
    });

    render(
      <MemoryRouter>
        <Question
          id="q1"
          title="Test Title"
          description="Test Description"
          createdAt="2024-01-01T00:00+00:00" // ISO 8601 UTC
          answered={false}
          author={{ id: "123", username: "Alice" }}
          onDelete={vi.fn()}
        />
      </MemoryRouter>
    );

    // Open answers section
    fireEvent.click(screen.getByTestId("OpenAnswerButton"));

    // Enter text in textarea
    const textarea = await screen.getByTestId("comment");
    fireEvent.change(textarea, { target: { value: "My new answer" } });

    // Click post button
    const postBtn = screen.getByText(/post/i);
    fireEvent.click(postBtn);

    // Wait for new answer to appear
    await waitFor(() => {
      expect(screen.getByText("My new answer")).toBeInTheDocument();
    });

    // Ensure axios.post was called
    expect(axios.post).toHaveBeenCalled();
  });
});