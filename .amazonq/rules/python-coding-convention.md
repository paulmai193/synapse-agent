# Coding Convention cho Ứng dụng Python

## 1. Quy tắc đặt tên biến, hàm, lớp

- **Biến và hàm:** Sử dụng kiểu `snake_case`, tên phải rõ nghĩa.
  - Ví dụ: `user_name`, `calculate_total()`
- **Lớp:** Sử dụng kiểu `PascalCase`.
  - Ví dụ: `OrderProcessor`, `CustomerProfile`
- **Hằng số:** Sử dụng kiểu `UPPER_CASE`.
  - Ví dụ: `MAX_RETRIES`, `DEFAULT_TIMEOUT`
- **Tên file:** Sử dụng kiểu `snake_case`.
  - Ví dụ: `order_service.py`, `data_loader.py`

## 2. Bố cục file

- Đầu file: Import các thư viện chuẩn trước, sau đó là thư viện bên ngoài, cuối cùng là import nội bộ (theo thứ tự alphabet).
- Tiếp theo là các hằng số cấu hình.
- Định nghĩa class, function.
- Đoạn code thực thi (nếu có) đặt trong khối `if __name__ == "__main__":`.
- Một file nên tập trung một chức năng chính (Single Responsibility Principle).

**Ví dụ bố cục:**
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

## 3. Cách viết docstring

- Sử dụng docstring cho module, class, function.
- Docstring phải mô tả chức năng, tham số, giá trị trả về và ngoại lệ (nếu có).
- Sử dụng định dạng [PEP 257](https://peps.python.org/pep-0257/).

**Ví dụ:**
```python
def calculate_total(price: float, tax: float) -> float:
    """
    Tính tổng giá trị sau thuế.

    Args:
        price (float): Giá gốc.
        tax (float): Thuế suất.

    Returns:
        float: Giá sau thuế.

    Raises:
        ValueError: Nếu price hoặc tax âm.
    """
    if price < 0 or tax < 0:
        raise ValueError("Price và tax phải không âm.")
    return price * (1 + tax)
```

## 4. Quy tắc indentation

- Sử dụng 4 dấu cách cho mỗi cấp indent (không dùng tab).
- Không indent dòng trống.
- Block lồng nhau nên hạn chế quá sâu (khuyến nghị không quá 3 cấp).

## 5. Cách xử lý lỗi

- Sử dụng `try`/`except` để bắt và xử lý ngoại lệ.
- Chỉ bắt các ngoại lệ cụ thể, tránh bắt chung chung (`except Exception`).
- Log lỗi đầy đủ thông tin (sử dụng module `logging`).
- Không che giấu lỗi, không để pass trống nếu không thực sự cần thiết.

**Ví dụ:**
```python
import logging

try:
    result = calculate_total(price, tax)
except ValueError as e:
    logging.error(f"Lỗi giá trị: {e}")
    raise
```

## 6. Quy tắc import

- Mỗi dòng chỉ import một module.
- Import chỉ ở đầu file, không import động trong function (trừ trường hợp đặc biệt).
- Sắp xếp import theo thứ tự: chuẩn → bên ngoài → nội bộ.
- Tránh import toàn bộ (`from module import *`).

## 7. Quy tắc comment

- Comment phải rõ ràng, đúng chỗ, tránh thừa thãi.
- Chỉ comment những đoạn code phức tạp hoặc logic đặc biệt.
- Sử dụng tiếng Việt hoặc tiếng Anh nhất quán trong dự án.
- Không dùng comment để “tắt” code, thay vào đó sử dụng git hoặc các công cụ quản lý version.

**Ví dụ:**
```python
# Tính toán giá trị trung bình, loại bỏ outlier
def compute_average(values):
    # Bỏ các giá trị lớn hơn 3 lần độ lệch chuẩn
    pass
```

## 8. Best Practices cho Python Application

- Tuân thủ PEP8 và PEP257.
- Sử dụng các công cụ kiểm tra code: `flake8`, `black`, `pylint`.
- Viết unit test (dùng `pytest` hoặc `unittest`), đặt test riêng trong thư mục `tests/`.
- Đảm bảo code dễ đọc, dễ bảo trì.
- Sử dụng type hinting cho function.
- Tránh hard-code dữ liệu trong code, sử dụng file cấu hình hoặc biến môi trường.
- Đảm bảo tài liệu nội bộ đầy đủ (README, HLD, API doc).
- Kiểm soát vòng lặp lồng nhau, tránh quá phức tạp.

---

## Tài liệu tham khảo

- [PEP 8 – Style Guide for Python Code](https://peps.python.org/pep-0008/)
- [PEP 257 – Docstring Conventions](https://peps.python.org/pep-0257/)
- [Google Python Style Guide](https://google.github.io/styleguide/pyguide.html)

---

**Lưu ý:** Coding convention này có thể điều chỉnh theo yêu cầu đặc thù của dự án hoặc tổ chức. Nên phổ biến và training cho tất cả các thành viên trước khi áp dụng.