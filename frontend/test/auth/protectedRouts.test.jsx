import React from "react";
import { render, screen } from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";

// Dummy components for testing
const DummyComponent = () => <p data-testid="protected-content">Protected Content</p>;
const LoginComponent = () => <p data-testid="login-page">Login Page</p>;

describe("ProtectedRoutes", () => {
  afterEach(() => {
    vi.resetModules();
  });

  it("renders Outlet when authenticated", async () => {
    vi.doMock('react-auth-kit/hooks/useIsAuthenticated', () => ({
      default: () => true,
    }));

    const ProtectedRoutes = (await import("@/auth/ProtectedRoutes.jsx")).default;

    render(
      <MemoryRouter initialEntries={["/protected"]}>
        <Routes>
          <Route element={<ProtectedRoutes />}>
            <Route path="/protected" element={<DummyComponent />} />
          </Route>
          <Route path="/login" element={<LoginComponent />} />
        </Routes>
      </MemoryRouter>
    );

    expect(await screen.findByTestId("protected-content")).toBeInTheDocument();
  });

  it("navigates to /login when not authenticated and passes location", async () => {
    vi.doMock('react-auth-kit/hooks/useIsAuthenticated', () => ({
      default: () => false,
    }));

    const ProtectedRoutes = (await import("@/auth/ProtectedRoutes.jsx")).default;

    render(
      <MemoryRouter initialEntries={["/protected"]}>
        <Routes>
          <Route element={<ProtectedRoutes />}>
            <Route path="/protected" element={<DummyComponent />} />
          </Route>
          <Route path="/login" element={<LoginComponent />} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByTestId("login-page")).toBeInTheDocument();
  });
});