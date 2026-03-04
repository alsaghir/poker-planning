{
  description = "Kotlin Multiplatform Project";

  inputs.devtools.url = "path:/home/ahmed/Projects/devtools";
  inputs.nixpkgs.follows = "devtools/nixpkgs";

  outputs = { self, nixpkgs, devtools, ... }:
    let
      system = "x86_64-linux";
    in
    {
      devShells.${system}.default = devtools.devShells.${system}.kotlin;
    };
}
