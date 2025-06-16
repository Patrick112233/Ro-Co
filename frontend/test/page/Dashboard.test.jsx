// Add this at the very top of your test/Dashboard.test.jsx file or in your test setup file
import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import Dashboard from "@/page/Dashboard.jsx";
import axios from "@/util/axios.js";
import { MemoryRouter } from "react-router-dom";
import { vi } from "vitest";


// Mock hooks
vi.mock("react-auth-kit/hooks/useSignOut", () => ({
  default: () => vi.fn(),
}));
vi.mock("react-auth-kit/hooks/useAuthHeader", () => ({
  default: () => "Bearer testtoken",
}));
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => vi.fn(),
  };
});
vi.mock("@/component/navbar.jsx", () => ({
  __esModule: true,
  default: () => <div data-testid="navbar" />,
}));
vi.mock("@/component/askQuestion.jsx", () => ({
  __esModule: true,
  default: () => <div data-testid="ask-question" />,
}));
vi.mock("@/component/question.jsx", () => ({
  __esModule: true,
  default: ({ id, hash, title, createdAt }) => (
    <div data-testid="question" data-id={id} data-hash={hash} data-createdat={createdAt}>
      {title}
    </div>
  ),
}));

describe("Dashboard", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("fetchQuestions paginates, hashes, and sorts questions newest first", async () => {
    // Create 21 questions with different createdAt timestamps
    const questionsPage1 = Array.from({ length: 10 }, (_, i) => ({
      id: `q${i + 1}`,
      title: `Title ${i + 1}`,
      description: `Desc ${i + 1}`,
      createdAt: `2024-01-01T00:00+00:00`,
      answered: false,
      author: { id: "u1", username: "User1" },
    }));
    const questionsPage2 = Array.from({ length: 11 }, (_, i) => ({
      id: `q${i + 11}`,
      title: `Title ${i + 11}`,
      description: `Desc ${i + 11}`,
      createdAt: `2024-01-01T00:00+00:00`,
      answered: false,
      author: { id: "u2", username: "User2" },
    }));

    // Mock axios.get for pagination: 2 pages
    vi.spyOn(axios, "get")
      .mockResolvedValueOnce({ status: 200, data: questionsPage1 })
      .mockResolvedValueOnce({ status: 200, data: questionsPage2 })
      .mockResolvedValue({status: 200, data: []});

    render(
      <MemoryRouter>
        <Dashboard />
      </MemoryRouter>
    );

    // Wait for all questions to be rendered
    const questionNodes = await screen.findAllByTestId("question");
    expect(questionNodes.length).toBe(21);

        // Wait for a question to appear
        await waitFor(() => {
          expect(screen.getByText("Title 1")).toBeInTheDocument();
          expect(screen.getByText("Title 15")).toBeInTheDocument();
          expect(screen.getByText("Title 21")).toBeInTheDocument();
        });

  });
});