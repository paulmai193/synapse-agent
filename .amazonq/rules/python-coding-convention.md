# 🐍 Python Application Coding Convention

## 1. Naming Rules

- **Variables and Functions**: Use `snake_case`, with meaningful names.  
  *Example*: `user_name`, `calculate_total()`

- **Classes**: Use `PascalCase`.  
  *Example*: `OrderProcessor`, `CustomerProfile`

- **Constants**: Use `UPPER_CASE`.  
  *Example*: `MAX_RETRIES`, `DEFAULT_TIMEOUT`

- **File Names**: Use `snake_case`.  
  *Example*: `order_service.py`, `data_loader.py`

---

## 2. File Structure

- **Top of File**:  
  - Import standard libraries first  
  - Then third-party libraries  
  - Finally internal modules (alphabetically)

- **Next**: Configuration constants

- **Then**: Class and function definitions

- **Execution Code**: Place inside `if __name__ == "__main__":`

- **Single Responsibility**: Each file should focus on one main function

**Example Layout**:
```python
import os
import sys
import requests
from utils import calculate_total

MAX_SIZE = 1024

class OrderProcessor:
    pass

def process_order():
    pass

if __name__ == "__main__":
    process_order()
```

---

## 3. Docstring Guidelines

- Use docstrings for modules, classes, and functions
- Must describe:
  - Purpose
  - Parameters
  - Return values
  - Exceptions (if any)
- Follow https://peps.python.org/pep-0257/

**Example**:
```python
def calculate_total(price: float, tax: float) -> float:
    """
    Calculate total price after tax.

    Args:
        price (float): Base price.
        tax (float): Tax rate.

    Returns:
        float: Final price after tax.

    Raises:
        ValueError: If price or tax is negative.
    """
    if price < 0 or tax < 0:
        raise ValueError("Price and tax must be non-negative.")
    return price * (1 + tax)
```

---

## 4. Indentation Rules

- Use **4 spaces** per indentation level (no tabs)
- Do not indent blank lines
- Avoid deep nesting (recommended: max 3 levels)

---

## 5. Error Handling

- Use `try`/`except` blocks
- Catch **specific exceptions** only
- Log errors with full context using `logging`
- Avoid silent failures or empty `pass` blocks unless necessary

**Example**:
```python
import logging

try:
    result = calculate_total(price, tax)
except ValueError as e:
    logging.error(f"Value error: {e}")
    raise
```

---

## 6. Import Rules

- One module per line
- All imports at the top of the file
- Avoid dynamic imports inside functions (unless necessary)
- Order: standard → third-party → internal
- Avoid wildcard imports (`from module import *`)

---

## 7. Commenting Guidelines

- Comments must be clear and relevant
- Only comment on complex or non-obvious logic
- Use either Vietnamese or English consistently across the project
- Do not use comments to disable code — use Git or version control tools

**Example**:
```python
# Calculate average value, excluding outliers
def compute_average(values):
    # Remove values greater than 3 standard deviations
    pass
```

---

## 8. Python Best Practices

- Follow https://peps.python.org/pep-0008/ and https://peps.python.org/pep-0257/
- Use code quality tools: `flake8`, `black`, `pylint`
- Write unit tests (`pytest` or `unittest`) and place in `tests/` folder
- Ensure code is readable and maintainable
- Use type hints for all functions
- Avoid hardcoded values — use config files or environment variables
- Maintain internal documentation (README, HLD, API docs)
- Avoid overly complex nested loops

---

## 📚 References

- https://peps.python.org/pep-0008/
- https://peps.python.org/pep-0257/
- https://google.github.io/styleguide/pyguide.html

---

**Note**: This coding convention may be adjusted based on project or organizational needs. It should be shared and trained across all team members before adoption.