# Explorer Robot: Hiện thực server

Giới thiệu
Trong mô hình, Server làm nhiệm vụ thu thập các dữ liệu do Raspberry gửi đi. Sau đó server sẽ lưu giữ những thông tin này vào trong Database và truy xuất các thông tin lưu giữ trong Database được yêu cầu bởi các thiết bị cần truy xuất dữ liệu thông qua API. Ngoài ra, Server còn cung cấp giao diện hiển thị các dự liệu thu thập được và ảnh thu thập được.
Cách hoạt động
Server: Được hiện thực bằng ngôn ngữ PHP và sử dụng Slim Framework.
Database: Nhóm sử dụng MySQL để hiện thực database. Nhóm thiết kể 01 table (datacollecting) để lưu tất cả những record lại. Mỗi record gồm có ID, Temperature , Humidity ,  Light , Gas ,  Time .Cấu trúc như sau:

CREATE TABLE datacollecting(
        ID int AUTO_INCREMENT,
    Temperature varchar(255),
    Humidity varchar(255),
    Light varchar(255),
    Gas varchar(255),
    Time TIMESTAMP,
    PRIMARY KEY (ID)
);

Để thuận tiện cho việc thao tác với database, nhóm sử dụng Eloquent ORM để hiện thực các thao tác, lấy thông tin, chèn thông tin vào database.
Nghiệm thu và hướng phát triển

Server đã hiện thực được thành công các chức năng:
Thu thập thông tin
Lưu ảnh thu thập trên server
 -    Nhưng hiện tại, khi deploy server trên webhoisting, nhóm đang gặp vấn đề về môi trường trong việc hiển thì ảnh thu thập được qua server và API đề lấy ảnh thu thập lưu trên server. Nhưng trên server local thì những chức năng này chạy bình thường. Trong tương lai nhóm sẽ hoàn thiện thêm các tính năng mở rộng.

## Hướng dẫn cài đặt

Server Local:

1. Cài đặt XAMPP
2. Copy source vào thự mục htdocs
3. Khởi động APACHE và MYSQL trên giao diện chương trình XAMPP
4. Server đã chạy tại địa chỉ 127.0.0.1 (Localhost).

Server live:

Địa chỉ: http://myiotserver2018.000webhostapp.com/

