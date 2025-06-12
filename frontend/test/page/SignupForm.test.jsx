import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import SignupForm from "@/page/SignupForm.jsx";
import axios from "@/util/axios.js";
import { vi } from "vitest";


const signInMock = vi.fn(() => true);
const navigateMock = vi.fn();

vi.mock("react-auth-kit/hooks/useSignIn", () => ({
  default: () => signInMock,
}));

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => navigateMock,
    useLocation: () => ({ state: { from: { pathname: "/dashboard" } } }),
  };
});

//vi.mock("@/assets/ROLIP_Logo.jpg", () => "logo.jpg");

// Mock axios
vi.mock("@/util/axios.js", () => ({
  __esModule: true,
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

describe("SignupForm", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("shows 'Username is already taken!' if username is unavailable", async () => {

    render(<SignupForm />);
    //go to signup
    fireEvent.click(screen.getByTestId("toggleBtn"));
    // Mock axios.get for username check (409)
    axios.get.mockRejectedValueOnce({ response: { status: 409 } });

    // Enter username
    fireEvent.change(screen.getByLabelText(/name:/i), { target: { value: "takenuser" } });

    await waitFor(() => {
      expect(screen.getByTestId("takenMsg")).toBeInTheDocument();
    });
  });

  it("toggles between login and signup forms", async () => {
    render(<SignupForm />);
    // Default is login
    await waitFor(() => {
        expect(screen.getByRole("heading", { name: /login/i })).toBeInTheDocument();
        expect(screen.getByLabelText(/mail/i)).toBeInTheDocument();
        expect(screen.queryByLabelText(/username:/i)).not.toBeInTheDocument(); // only login via email
    });

    // Switch to signup
    fireEvent.click(screen.getByTestId("toggleBtn"));
    expect(screen.getByText(/signup/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/name:/i)).toBeInTheDocument();

    // Switch back to login
    fireEvent.click(screen.getByTestId("toggleBtn"));
    await waitFor(() => {
        expect(screen.getByRole("heading", { name: /login/i })).toBeInTheDocument();
        expect(screen.queryByLabelText(/name:/i)).not.toBeInTheDocument();
    });
  });

it("submits login and calls signIn and navigate", async () => {
  signInMock.mockClear(); // reset calls
  navigateMock.mockClear(); // reset calls

  render(<SignupForm />);
  fireEvent.change(screen.getByLabelText(/mail:/i), { target: { value: "test@example.com" } });
  fireEvent.change(screen.getByLabelText(/password:/i), { target: { value: "Password1!" } });

  axios.post.mockResolvedValueOnce({
    data: {
      token: "token",
      refreshToken: "refresh",
      email: "test@example.com",
      role: "user",
      id: "1",
    },
  });

  fireEvent.click(screen.getByTestId("submit"));

  await waitFor(() => {
    expect(signInMock).toHaveBeenCalled();
    expect(navigateMock).toHaveBeenCalledWith("/dashboard", { replace: true });
  });
});

  it("submits signup, calls axios.post, signIn, and navigate", async () => {
      signInMock.mockClear();
      navigateMock.mockClear();


    render(<SignupForm />);
    fireEvent.click(screen.getByTestId("toggleBtn"));
    fireEvent.change(screen.getByLabelText(/name:/i), { target: { value: "newuser" } });
    fireEvent.change(screen.getByLabelText(/mail:/i), { target: { value: "new@example.com" } });
    fireEvent.change(screen.getByLabelText(/^password:/i), { target: { value: "Password1!" } });
    fireEvent.change(screen.getByLabelText(/confirm password:/i), { target: { value: "Password1!" } });

    // Mock axios.post for signup and login
    axios.post
      .mockResolvedValueOnce({}) // signup
      .mockResolvedValueOnce({
        data: {
          token: "token",
          refreshToken: "refresh",
          email: "new@example.com",
          role: "user",
          id: "2",
        },
      }); // login

    fireEvent.click(screen.getByTestId("submit"));

    await waitFor(() => {
      expect(axios.post).toHaveBeenCalledWith(
        "auth/signup",
        expect.objectContaining({
          username: "newuser",
          email: "new@example.com",
          password: "Password1!",
        }),
        expect.any(Object)
      );
      expect(signInMock).toHaveBeenCalled();
      expect(navigateMock).toHaveBeenCalledWith("/dashboard", { replace: true });
    });
  });

  it("shows invalid email and password messages in signup", async () => {
    render(<SignupForm />);
    fireEvent.click(screen.getByTestId("toggleBtn"));

    // Invalid email
    fireEvent.change(screen.getByLabelText(/mail:/i), { target: { value: "notanemail" } });
    fireEvent.blur(screen.getByLabelText(/mail:/i));
    expect(screen.getByLabelText(/mail:/i)).toHaveClass("is-invalid");

    // Invalid password
    fireEvent.change(screen.getByLabelText(/^password:/i), { target: { value: "short" } });
    fireEvent.blur(screen.getByLabelText(/^password:/i));
    expect(screen.getByLabelText(/^password:/i)).toHaveClass("is-invalid");

    // Password mismatch
    fireEvent.change(screen.getByLabelText(/confirm password:/i), { target: { value: "different" } });
    fireEvent.blur(screen.getByLabelText(/confirm password:/i));

    expect(screen.getByTestId("pwdMissmatch")).toBeInTheDocument();
  });
});