# ModelPhy

ModelPhy is a language for interchange of phylogenetic models between different software packages. It provides a clear, concise syntax for defining probabilistic models of sequence evolution, tree priors, and observed data.

## Overview

Phylogenetic analyses often require setting up complex models that include multiple components:
- Substitution models (e.g., JC69, HKY, GTR)
- Rate heterogeneity models (e.g., Gamma, invariant sites)
- Tree priors (e.g., Yule, Birth-Death)
- Clock models (e.g., strict clock, relaxed clock)

Currently, each software package (MrBayes, BEAST, RevBayes, etc.) has its own syntax for specifying these models, making it difficult to share models between researchers and platforms. ModelPhy aims to solve this problem by providing a standard language that can be translated to and from different software packages.

## Features

- Human-readable syntax for defining phylogenetic models
- Mathematical types corresponding to phylogenetic concepts
- Stochastic assignments for random variables
- Deterministic assignments for derived quantities
- Support for common probability distributions
- Observations for tying models to data

## Example

```
// Define transition transversion ratio prior
real kappa ~ lognormal(mean=1.0, sigma=0.5);

// Define nucleotide frequency prior
simplex pi ~ dirichlet(alpha=[1.0, 1.0, 1.0, 1.0]);

// Create HKY substitution model
substmodel subst_model = hky(kappa=kappa, freqs=pi);

// Define birth rate and create Yule tree prior
real birth_rate ~ exponential(mean=0.1);
timetree phylogeny ~ yule(birthrate=birth_rate, n=3);

// Create phylogenetic CTMC model
alignment seq ~ phyloCTMC(tree=phylogeny, substmodel=subst_model);

// Attach observed sequence data
seq observe [ 
  human = sequence(str="ACGTACGTACGTACGTACGTACGT"),
  chimp = sequence(str="ACGTACGTACGTACGTATGTACGT"),
  gorilla = sequence(str="ACGTACGTACGCACGTACGTACGT")
];
```

## Repository Structure

- `/spec`: Language specification and documentation
- `/grammar`: ANTLR grammar files
- `/java`: Java implementation of ModelPhy parser
- `/cpp`: C++ implementation of ModelPhy parser
- `/examples`: Example ModelPhy files
- `/converters`: Converters to/from other formats (BEAST XML, RevBayes scripts, etc.)

## Getting Started

### Prerequisites

- Java 11 or higher
- ANTLR 4.9 or higher
- CMake 3.10 or higher (for C++ implementation)

### Building from Source

```bash
# Clone the repository
git clone https://github.com/yourusername/modelphy.git
cd modelphy

# Build Java implementation
cd java
./gradlew build

# Build C++ implementation
cd ../cpp
mkdir build && cd build
cmake ..
make
```

### Basic Usage

```bash
# Parse and validate a ModelPhy file
java -jar modelphy.jar validate example.mph

# Convert ModelPhy to BEAST XML
java -jar modelphy.jar convert --to beast example.mph > example.xml

# Convert ModelPhy to RevBayes script
java -jar modelphy.jar convert --to revbayes example.mph > example.rev
```

## Language Specification

The full language specification is available in [SPECIFICATION.md](./spec/SPECIFICATION.md).

## Roadmap

- [ ] Complete ANTLR grammar
- [ ] Java reference implementation
- [ ] C++ implementation
- [ ] Converter to/from BEAST XML
- [ ] Converter to/from RevBayes
- [ ] Support for more complex models (partitioned analyses, relaxed clocks)
- [ ] Web-based model builder and validator

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- This project was inspired by the need for better interoperability between phylogenetic software packages
- Thanks to the developers of BEAST, MrBayes, RevBayes, and other phylogenetic software for their pioneering work
