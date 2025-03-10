# Reserve Seat
회사에서 솔루션 개발 당시 Redis를 사용하게 되어 Redis에 대해 알아보며, 동시성 관련 Inflearn을 수강하여 두개를 접목하여 간단한 Toy Project를 만들어 익히며 학습하고자 프로젝트를 수행하였다.  
그 중 좌석 영화관 좌석 예매 사이트를 구현하고자 하는 이유는,  
여러 사람이 접근하면서, 순차적으로 일정인원만 사용할 수 있도록 서비스를 제공해야 하므로 Redis와 동시성을 익히는데 좋은 예제라 생각했기 때문이다

# 요구 사항
 - 좌석 예약을 위해 사용자가 자신의 이름을 입력하여여 좌석 선택 페이지로 이동한다.
 - 선착순으로 제한된 인원만 좌석 예약 화면으로 이동할수 있으며 제한된 인원을 초과하는 인원은 좌석 예약 화면으로 이동하지 못하고 대기하게 된다.
 - 이동한 인원은 일정 시간(1분) 이후에는 자동으로 처음 화면으로 이동하게 한다.
 - 좌석을 선택한 후 다른 사람이 예약을 먼저 진행하였을 경우 '이미 선점된 좌석입니다'를 표시한다.
 - 예약을 성공하면 예약 확인 화면으로 이동하여 예약 좌석을 확인 한다.
 - 정상적인 시나리오를 거치지 않고 Get 으로 화면 이동을 요청하는 경우를 위해 페이지 이동시 매번 저장된 데이터를 조회해 Home 화면으로 이동시킨다.

# 시나리오
 - 대기 신청
  ![image](https://github.com/user-attachments/assets/dcde90e7-3594-46d6-9266-c508d9df07bd)  
 - 좌석 페이지 이동  
  ![image](https://github.com/user-attachments/assets/93275be5-c4e0-41a8-928c-e7fbf69b7d91)  
 - 좌석 예약  
  ![image](https://github.com/user-attachments/assets/8df3fcc5-e6ca-4d53-9920-d5c23cf5c523)  

# 사용 기술  
Environment  
 - intellij, git, docker  
development  
 - java, Spring boot, js, mysql, redis, JPA  

# 화면 구성
 - Home 화면  
  ![image](https://github.com/user-attachments/assets/896e1252-ca08-40bf-8379-d4a035154342)  
 - 좌석 선택 화면  
  ![image](https://github.com/user-attachments/assets/3de0ad66-de4c-4067-b361-647918e28b54)  
 - 예약 확인 화면  
  ![image](https://github.com/user-attachments/assets/fc0f4f6f-38e9-41b9-ae34-2e52b27f0b9c)  

# 추후
 - 로그인 기능 구현  
 - 로그인 한 session 으로 예약 진행 Process  
 - 여러 영화 선택 기능  
