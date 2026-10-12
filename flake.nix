{
  description = "Lift Shiro Dev Shell";

  inputs = {
    nixpkgs.url     = "github:NixOS/nixpkgs/26.05";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };

        jdk = pkgs.jdk8;
        sbt = pkgs.sbt.override {
          jre = jdk;
        };
      in
      {
        devShells.default = pkgs.mkShell {
          name = "lift-shiro-dev-shell";

          packages = [
            jdk
            sbt
          ];
        };
      }
    );
}