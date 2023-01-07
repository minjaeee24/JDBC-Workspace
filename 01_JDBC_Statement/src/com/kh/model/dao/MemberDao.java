package com.kh.model.dao;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import com.kh.model.vo.Member;

/*
 *  DAO(Date Access Object)
 *  Controller를 통해 호출
 *  Controller에서 요청받은 실질적인 기능을 수행함
 *  DB에 직접 접근해서 SQL문을 실행하고, 수행결과 돌려받기 -> JDBC
 */
public class MemberDao {
	/*
	 * 기존의 방식 : DAO클래스에 사용자가 요청할때마다 실행해야되는 SQL문을 자바 소스코드 내에 직접 명시적으로 작성함
	 * 			=> 정적 코딩방식, 하드코딩
	 * 문제점 : SQL구문을 수정해야할 경우 자바 소스코드를 수정하는 셈. 즉, 수정된 내용을 반영시키고자 한다면
	 * 		  프로그램을 재구동해야함.
	 * 해결방식 : SQL문들을 별도로 관리하는 외부포일(.XML)을 만들어서 실시간으로 이 파일에 기록된 SQL문들을 동적으로
	 * 			읽어들여서 실행 => 동적 코딩방식
	 * 
	 */

	private Properties prop = new Properties();
	
	public MemberDao() {
		
		try {
			prop.loadFromXML(new FileInputStream("resources/query.xml"));
		} catch (InvalidPropertiesFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	/*
	 * JDBC용 객체
	 * - Connection : DB와의 연결정보를 담고 있는 객체(IP주고, PORT번호, 계정명, 비밀번호)
	 * - (Prepared)Statement : 해당 DB에 SQL문을 전달하고 실행한 후 결과를 받아내는 객체
	 * - ResultSet : 만일 실행한 SQL문이 SELECT문일 경우 조회된 결과들이 담겨있는 객체
	 * 
	 * JDBC 처리 순서
	 * 1) JDBC DRIVER 등록 : 해당 DBMS가 제공하는 클래스 등록
	 * 2) Connection 생성 : 접속하고자 하는 DB정보를 입력해서 DB에 접속하면서 생성
	 * 3) Statement 생성 : Connection객체를 이용해서 생성
	 * 4) SQL문을 전달하면서 실행 : Statement 객체를 이용해서 SQL문 실행
	 * 						  > SELECT문일 경우 - executeQuery()메소드를 이용하여 실행
	 * 						  > 기타 DML문일 경우 - executeUpdate()메소드를 이용하여 실행
	 * 5) 결과 받기
	 * 						  > SELECT문일 경우 -> ResultSet객체로 받기 => 6_1)
	 * 						  > 기타 DML문일 경우 -> INT형 변수(처리된 행의 개수)로 받기 => 6_2)
	 * 6_1) ResultSet(조회된 데이터들)객체에 담긴 데이터들을 하나씩 뽑아서 vo객체로 만들기(arrayList로 묶어서 관리)
	 * 6_2) 트랜젝션 처리(성공이면 Commit, Rollback)
	 * 7) 다 쓴 JDBC용 객체들을 반납(close()) -> 생성된 순서의 역순으로 반납
	 * 8) 결과를 Controller에게 반환
	 * 		> select문일 경우 6_1)에서 만들어진 결과값 반환
	 * 		> 기타 DML문일 경우 - int형 값(처리된 행의 개수)를 반환
	 * 
	 * * Statement특징 : 완성된 SQL문을 실행할 수 있는 객체
	 */
	
	
	/**
	 * 사용자가 회원 추가 요청시 입력했던 값을 가지고 Insert문을 실행하는 메소드
	 * @param m : 사용자가 입력했던 아이디부터 취미까지의 값을 가지고 만든 vo객체
	 * @return : Insert문을 실행한 행의 결과값
	 */
	public int insertMember(Member m) {
		// Insert문 -> 처리된 행의 개수 -> 트랜젝션 처리
		
		// 0) 필요한 변수 세팅
		int result = 0; // 처리된 결과(처리된 행의 개수)를 담아줄 변수
		Connection conn = null; // 접속된 DB의 연결정보를 담는 변수
		Statement stmt = null; // SQL문 실행 후 결과를 받기 위한 변수
		
		//+ 필요한 변수 : 실행시킬 SQL문(완성된 형태의 SQL문으로 만들기) => 끝에 세미콜론 절대 붙이지 말기.
		/*
		 * INSERT INTO MEMBER
		 * VALUES (SEQ_USERNO.NECTVAL, 'XXX', 'XXX', 'XXX', 'X', XX, 'XX@XXXX', 'XXX', 'XXXX', 'XXX', DEFAULT)
		 * 
		 */
		String sql = "INSERT INTO MEMBER VALUES(SEQ_USERNO.NEXTVAL,"
				+ "'" + m.getUserId() + "', "
				+ "'" + m.getUserPwd() + "', "
				+ "'" + m.getUserName() + "', "
				+ "'" + m.getGender() + "', "
				+		m.getAge() + ", "
				+ "'" + m.getEmail() + "', "
				+ "'" + m.getPhone() + "',"
				+ "'" + m.getAddress() + "',"
				+ "'" + m.getHobby() + "',"
				+ "DEFAULT)";
		
		try {
			// 1) JDBC드라이버 등록
			Class.forName("oracle.jdbc.driver.OracleDriver");
			// 오타가 있을 경우, ojdbc6.jar이 없을 경우 -> ClassNotFoundException이 발생함.
			
			// 2) Connection객체 생성 -> DB와 연결시키겠다
			conn = DriverManager.getConnection("jdbc:oracle:thin:@khacademyDB1_medium?TNS_ADMIN=/Users/minjaeee/Desktop/dev");
			//String url = "jdbc:oracle:thin:@khacademyDB1_medium?TNS_ADMIN=/Users/minjaeee/Desktop/dev";
			//String user = "ADMIN";
			//String password = "alswoWkdWkd123";
			//conn = DriverManager.getConnection(url, user, password);
			// 3) Statement 객체 생성
			stmt = conn.createStatement();
			
			// 4, 5) DB에 완성된 SQL문을 전달하면서 실행 후 결과 받기
			result = stmt.executeUpdate(sql);
			
			// 6_2) 트랜젝션 처리
			if(result > 0) { // 1개 이상의 행이 INSERT되었다면 => 커밋
				conn.commit();
			}else { // 실패했을 경우 => 롤백
				conn.rollback();
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// 7) 다 쓴 자원 반납해주기 -> 생성된 순서의 역순으로
			try {
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
		
			}
		}
		// 8) 결과 반환
		return result;
	}
	
	/**
	 * 사용자가 회원 전체 조회 요청시 select문을 실행해주는 메소드
	 * @return
	 */
	public ArrayList<Member> selectAll() {
		// SELECT -> ResultSet => ArrayList로 반환
		
		// 0) 필요한 변수들 세팅
		// 조회된 결과를 뽑아서 담아줄 변수 => ArrayList<Member> -> 여러 회원에 대한 정보.
		ArrayList<Member> list = new ArrayList<>(); // 현재 텅빈 리스트
		
		// Connection, Statement, ResultSet
		Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null; // SELECT문이 실행된 조회결과값들이 처음에 실질적으로 담길 객체
		
		String sql = "SELECT * FROM MEMBER";
		
		try {
			// 1) JDBC드라이버 등록
			Class.forName("oracle.jdbc.driver.OracleDriver");
			// 오타가 있을 경우, ojdbc6.jar이 없을 경우 -> ClassNotFoundException이 발생함.
			
			// 2) Connection객체 생성 -> DB와 연결시키겠다
			conn = DriverManager.getConnection("jdbc:oracle:thin:@khacademyDB1_medium?TNS_ADMIN=/Users/minjaeee/Desktop/dev");
		
			// 3) Statement객체 생성
			stmt = conn.createStatement();
			
			// 4, 5)
			rset = stmt.executeQuery(sql);
			
			// 6_1) 현재 조회결과가 담긴 ResultSet에서 한 명씩 뽑아서 vo객체에 담기
			// rset.next() : 커서를 한 줄 아래로 옮겨주고 해당 행이 존재할 경우 true, 아니면 false를 반환해주는 메소드
			while(rset.next()) {
				
				// 현재 rset의 커서가 가리키고 있는 해당 행의 데이터를 하나씩 뽑아서 Member객체 담기
				Member m = new Member();
				
				// rset으로부터 어떤 칼럼에 있는 값을 뽑을것인지 제시
				// 칼럼명(대소문자x), 칼럼순번
				// 권장사항 : 칼럼명으로 쓰고, 대문자로 쓰는 것을 권장함
				// rset.getInt(칼럼명 또는 순번) : int형 값을 뽑아낼때 사용
				// rset.getString(칼럼명 또는 칼럼순번) : String값을 뽑아낼때 사용
				// rset.getDate(칼럼명 또는 칼럼순번) : Date값을 뽑아올때 사용하는 메소드
				
				m.setUserNo(rset.getInt("USERNO"));
				m.setUserId(rset.getString("USERID"));
				m.setUserPwd(rset.getString("USERPWD"));
				m.setUserName(rset.getString("USERNAME"));
				m.setGender(rset.getString("GENDER"));
				m.setAge(rset.getInt("AGE"));
				m.setEmail(rset.getString("EMAIL"));
				m.setPhone(rset.getString("PHONE"));
				m.setAddress(rset.getString("ADDRESS"));
				m.setHobby(rset.getString("HOBBY"));
				m.setEnrollDate(rset.getDate("ENROLLDATE"));
				// 한 행에 대한 모든 칼럼의 데이터값들을
				// 각각의 필드에 담아 하나의 Member객체에 옮겨담아주기 끝
				
				list.add(m); // 리스트에 해당 Member객체를 담아주기
				
			}
			
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
			try {
				rset.close();
				stmt.close();
				conn.close();
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	
	public Member selectByUserId(String userId) {
		
		// 0) 필요한 변수 셋팅
		// 조회된 회원에 대한 정보를 담을 변수
		Member m = null;
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;
		
		// 실행할 sql문(완성된 형태, 세미콜론 X)
		String sql = "SELECT * FROM MEMBER WHERE USERID = '" + userId + "'";
		
		try {
			// 1) JDBC드라이버 등록
			Class.forName("oracle.jdbc.driver.OracleDriver");
			// 오타가 있을 경우, ojdbc6.jar이 없을 경우 -> ClassNotFoundException이 발생함.
			
			// 2) Connection객체 생성 -> DB와 연결시키겠다
			conn = DriverManager.getConnection("jdbc:oracle:thin:@khacademyDB1_medium?TNS_ADMIN=/Users/minjaeee/Desktop/dev");
		
			// 3) Statement객체 생성
			stmt = conn.createStatement();
			
			// 4, 5) SQL문 실행시켜서 결과 받기
			rset = stmt.executeQuery(sql);
			
			// 6_1) 현재 조회결과가 담긴 ResultSet에서 한 행씩 뽑아서 VO객체에 담기 => ID검색은 검색결과가 한 행이거나, 한 행도 없을 것
			if(rset.next()) { // 커서를 한 행 아래로 슬쩍 움직여보고 조회결과가 있다면 true, 없다면 false
				
				// 조회된 한 행에 대한 모든 열의 데이터값을 뽑아서 하나의 Member객체에 담기
				m = new Member(rset.getInt("USERNO"),
							   rset.getString("USERID"),
							   rset.getString("USERPWD"),
							   rset.getString("USERNAME"),
							   rset.getString("GENDER"),
							   rset.getInt("AGE"),
							   rset.getString("EMAIL"),
							   rset.getString("PHONE"),
							   rset.getString("ADDRESS"),
							   rset.getString("HOBBY"),
							   rset.getDate("ENROLLDATE"));
				
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rset.close();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return m;
	}
	
	
	
	
	
}
