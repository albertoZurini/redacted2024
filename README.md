# NFC Demo Application

## Mô tả

Đây là một ứng dụng Android mẫu, thực hiện tính năng đọc và ghi dữ liệu từ/đến thẻ NFC. Ứng dụng này được xây dựng bằng `Jetpack Compose` và sử dụng `NfcAdapter` để tương tác với các thẻ NFC. Bạn có thể chuyển đổi giữa chế độ đọc và chế độ ghi dữ liệu từ các thẻ NFC.

## Các tính năng chính

### 1. Đọc dữ liệu từ thẻ NFC
Ứng dụng có thể phát hiện các thẻ NFC và đọc nội dung của các bản ghi NDEF (NFC Data Exchange Format). Khi một thẻ NFC được đưa vào phạm vi đọc, ứng dụng sẽ trích xuất dữ liệu từ thẻ và hiển thị cho người dùng.

- **Hành vi**: Khi không ở chế độ ghi, ứng dụng sẽ tự động đọc thẻ NFC khi thẻ được phát hiện.
- **Dữ liệu đọc được**: Ứng dụng sẽ lấy nội dung từ bản ghi NDEF đầu tiên trong thẻ và hiển thị lên giao diện người dùng.

### 2. Ghi dữ liệu vào thẻ NFC
Ứng dụng cho phép người dùng nhập dữ liệu văn bản và ghi dữ liệu này vào thẻ NFC khi ở chế độ ghi. Khi người dùng chọn chế độ ghi, nhập nội dung cần ghi và đưa thẻ NFC vào phạm vi đọc, dữ liệu sẽ được ghi vào thẻ.

- **Hành vi**: Khi ở chế độ ghi, người dùng nhập nội dung và đưa thẻ NFC vào vùng đọc. Nếu ghi thành công, ứng dụng sẽ thông báo cho người dùng.
- **Dữ liệu ghi**: Ứng dụng ghi dữ liệu vào thẻ dưới dạng bản ghi NDEF với mã ngôn ngữ mặc định là `en` (tiếng Anh).

### 3. Chuyển đổi chế độ
Người dùng có thể dễ dàng chuyển đổi giữa hai chế độ đọc và ghi bằng cách nhấn nút "Chuyển sang chế độ ghi" hoặc "Chuyển sang chế độ đọc".

- **Chế độ đọc**: Mặc định, ứng dụng sẽ đọc dữ liệu từ thẻ NFC.
- **Chế độ ghi**: Khi chuyển sang chế độ ghi, người dùng sẽ có thể nhập nội dung và ghi vào thẻ NFC.

## Hướng dẫn sử dụng

1. **Mở ứng dụng**: Ứng dụng sẽ mặc định ở chế độ đọc.
2. **Đọc thẻ NFC**: Đưa thẻ NFC vào phạm vi đọc của thiết bị. Nếu thẻ có chứa dữ liệu NDEF, nội dung sẽ được hiển thị.
3. **Ghi thẻ NFC**:
   - Nhấn vào nút "Chuyển sang chế độ ghi".
   - Nhập nội dung bạn muốn ghi vào thẻ.
   - Đưa thẻ NFC vào phạm vi đọc của thiết bị để ghi dữ liệu.
4. **Chuyển đổi chế độ**: Bạn có thể nhấn vào nút chuyển đổi để chuyển giữa chế độ đọc và ghi.

## Cấu trúc dự án

- **MainActivity.kt**: Đây là nơi xử lý logic chính của ứng dụng, bao gồm việc tương tác với NFC và cập nhật giao diện.
  - `onNewIntent`: Được gọi khi thiết bị phát hiện thẻ NFC.
  - `readNfcTag`: Hàm để đọc dữ liệu từ thẻ NFC.
  - `writeNfcTag`: Hàm để ghi dữ liệu vào thẻ NFC.
  
- **NFCReaderWriter.kt**: Chứa thành phần giao diện `Jetpack Compose` để hiển thị giao diện người dùng cho việc đọc và ghi NFC.

## Yêu cầu

- Thiết bị Android hỗ trợ NFC.
- NFC phải được bật trong cài đặt của thiết bị.

## Ghi chú

- Ứng dụng chỉ hỗ trợ các thẻ NFC chứa dữ liệu theo định dạng NDEF.
- Khi ghi dữ liệu vào thẻ, ứng dụng sẽ ghi một bản ghi NDEF với ngôn ngữ mặc định là "en" (tiếng Anh).
- Một số thẻ NFC có thể không hỗ trợ ghi hoặc có thể bị khóa, trong trường hợp này quá trình ghi sẽ thất bại.
