
import { render, screen } from "@testing-library/react";
import CustomNavbar from "@/component/Navbar.jsx";
import axios from "@/util/axios.js";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import handleErrorLogout from "@/util/ErrorHandler.jsx";


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

beforeAll(() => {
  global.URL.createObjectURL = vi.fn(() => "mocked-url");
});

afterAll(() => {
  global.URL.createObjectURL.mockRestore?.();
});

describe("Navbar", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("should render with user name", async () => {
    vi.spyOn(axios, "get")
      .mockImplementationOnce(() => Promise.resolve({ status: 200, data: { username: "Alice" } }))
      .mockImplementationOnce(() => Promise.resolve({ status: 200, data: "Image Data" }));

    render(
      <MemoryRouter>
        <CustomNavbar />
      </MemoryRouter>
    );

    const userName = await screen.findByText("Alice");
    expect(userName).toBeInTheDocument();
  });

  it("should load and display the profile icon", async () => {
    vi.spyOn(axios, "get")
      .mockImplementationOnce(() => Promise.resolve({ status: 200, data: { username: "Alice" } }))
      .mockImplementationOnce(() => Promise.resolve({ status: 200, data: "Image Data" }));

    render(
      <MemoryRouter>
        <CustomNavbar />
      </MemoryRouter>
    );

    const images = screen.queryAllByRole("img");
    images.forEach(image => {
       if (image.getAttribute("alt") === "User Avatar") {
          expect(image.getAttribute("src")).not.toBe("");
       }
     });

  });


  it("should call handleErrorLogout if axios.get throws", async () => {

  vi.spyOn(axios, "get").mockImplementation(() => Promise.reject(new Error("Network Error")));
  render(
    <MemoryRouter>
      <CustomNavbar />
    </MemoryRouter>
  );

  // Wait for the effect to run
  await screen.findAllByRole("img"); // Wait for all images to render

  expect(handleErrorLogout).toHaveBeenCalled();
});

});