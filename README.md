# Hyunndystagram


1. TextInputLayout은 EditView를 좀 더 유연성있게 쓰기 위해 고안된 Layout.

2. EditText의 android::hint 프로퍼티는 텍스트 입력전에 희미하게 써있는것.

3. values폴더에 xml 파일을 추가해서 layout에서 불러쓸 수 있다.
EX) strings.xml에있는 Email 값을 android:hint="@string/email" 처럼 쓸 수 있다.

4. 앱 - 파이어베이스 연동 방법
https://firebase.google.com/docs/android/setup?hl=ko

5. FirebaseAuth
Firebase 인증 Android 라이브러리
https://firebase.google.com/docs/auth/android/start?hl=ko

FirebaseAuth.currentUser는 현재 들어와있는 계정정보

6. Firebase를 통해 로그인하려면
Authentication에서 로그인 방법에 대한 권한을 추가해줘야한다.

7. 구글 로그인
구글 로그인은 3단계로 이루어져 있다.
구글로 접속해서 로그인 -> Firebase에서 로그인 -> 로그인 처리

7-1) 구글로 접속해서 로그인
googleLogin()에서 googleSignInClient?.signInIntent를 받아와 startActivityForResult를 호출한다.
onActivityResult()에서 Auth.GoogleSignInApi.getSignInResultFromIntent(data)가 성공적으로 온다면,
구글api로부터 온  result.signInAccount의 idToken을 이용해서 

7-2) 파이어베이스로 로그인
GoogleAuthProvider.getCredential(account?.idToken, null) 신임장을 받아온다.
파이어베이스 인증 라이브러리에서  auth?.signInWithCredential(credential)로 로그인을 시도한 후,
complete하면 메인 페이지로 이동한다.

8. gradle 에서 직접 Firebase를 추가했기 때문에 gradle -> task -> android -> shinigreport를 클릭해서 나온
sha-1 인증코드를 firebase에 넣어주어야 한다.

9. BottomNavigationView 하단의 네비게이션바.
BottomNavigationView에서 사용할 menu.xml을 생성하고, 
BottomNavigationView을 사용할 xml의 layout resource에 BottomNavigationView을 정의해야한다.

10.BottomNavigationView의 리스너.
class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

11. photoPickerIntent.type = "image/*"
Intent에 type을 지정해서 원하는 데이터에만 접근.. 갤러리라면 image/*.

12. Firebasestore에 Url로 사진 업로드 하는 법. 참조객체를 가져와서 한다.

        // Firebasestorage 참조 변수를 선언해서, child(storage 저장폴더명).child(파일이름_)를 정해준다.
        var storageRef = storage?.reference?.child("images")?.child(imageFilename)

        // 저장폴더명, 파일이름에 Url을 통해 실제 파일을 DB에 넣어준다!
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            Toast.makeText(this, "이미지 업로드에 성공했습니다.", Toast.LENGTH_LONG).show()

13. FirebaseFirestore는 진짜 DB/ FirebaseStorage는 데이터를 저장하는 버킷.

14. Firebase에 이미지를 업로드하는 방식에는 1.PromiseMethod , 2. CallbackMethod가 있는데 구글이 권장하는건 Promise
