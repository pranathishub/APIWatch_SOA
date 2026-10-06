import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent, cleanup } from "@testing-library/react";
import { afterEach } from "vitest";
import { Badge, Pagination, ErrorMessage } from "./UI";
import { ms, percent, time } from "../utils/format";
afterEach(cleanup);
describe("monitor display", () => {
  it("distinguishes unknown values from real zeros", () => {
    expect(ms(null)).toBe("—");
    expect(ms(0)).toBe("0 ms");
    expect(percent(null)).toBe("—");
    expect(percent(0)).toBe("0.00%");
    expect(time(null)).toBe("Not checked yet");
  });
  it("renders DOWN status clearly", () => {
    render(<Badge status="DOWN" />);
    expect(screen.getByText("DOWN")).toHaveClass("down");
  });
  it("prevents going past pagination boundaries", () => {
    const change = vi.fn();
    render(<Pagination page={0} totalPages={2} setPage={change} />);
    expect(screen.getByText("Previous")).toBeDisabled();
    fireEvent.click(screen.getByText("Next"));
    expect(change).toHaveBeenCalledWith(1);
  });
  it("announces errors and supports retry", () => {
    const retry = vi.fn();
    render(<ErrorMessage message="Connection failed" retry={retry} />);
    expect(screen.getByRole("alert")).toHaveTextContent("Connection failed");
    fireEvent.click(screen.getByText("Try again"));
    expect(retry).toHaveBeenCalledOnce();
  });
});
