import { useMemo } from "react";
import { useAuth } from "../context/AuthContext";

export default function Greeting({ className = "" }) {
  const { user } = useAuth();

  const content = useMemo(() => {
    if (!user) {
      return "Welcome";
    }

    if (user.role === "ADMIN") {
      return "Welcome back, Admin";
    }

    const name = user.name || user.email?.split("@")[0];
    if (!name) {
      return "Welcome";
    }

    return (
      <>
        Hello{" "}
        <span className="font-semibold text-accent">{name}</span>
      </>
    );
  }, [user]);

  return (
    <div
      className={`text-sm sm:text-base animate-fade ${className}`.trim()}
    >
      {content}
    </div>
  );
}
