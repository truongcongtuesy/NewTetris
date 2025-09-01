# 🎮 Hướng Dẫn Chạy Game Tetris

## Bước 1: Tải Game
- Vào link: **https://github.com/truongcongtuesy/NewTetris**
- Nhấn nút **"Code"** màu xanh → chọn **"Download ZIP"**
- Giải nén file vừa tải về

## Bước 2: Kiểm Tra Java
Mở **Command Prompt** (gõ `cmd` trong Start Menu) và gõ:
```
java -version
```
- ✅ Nếu hiện thông tin Java → OK, chuyển bước 3
- ❌ Nếu báo lỗi → Tải Java tại: https://www.oracle.com/java/technologies/downloads/

## Bước 3: Chạy Game
1. Mở thư mục game đã giải nén
2. **Shift + Click chuột phải** trong thư mục → chọn **"Open PowerShell window here"**
3. Gõ 2 lệnh này (từng lệnh một):
   ```
   javac TetrisGame.java
   ```
   ```
   java TetrisGame
   ```

## 🎯 Xong! Game sẽ mở!

### Cách Chơi:
- **↑↓** - Chọn menu
- **Enter** - Xác nhận
- **,/.** - Di chuyển khối trái/phải
- **L** - Xoay khối
- **Space** - Thả nhanh
- **P** - Tạm dừng
- **M** - Bật/tắt nhạc
- **Ctrl+S** - Bật/tắt âm thanh
- **ESC** - Về menu chính

---
💡 **Lưu ý**: Sau lần đầu compile, lần sau chỉ cần chạy `java TetrisGame` là đủ!
