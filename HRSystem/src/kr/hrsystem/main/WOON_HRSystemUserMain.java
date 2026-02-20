package kr.hrsystem.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kr.hrsystem.dao.MemberDAO;

public class WOON_HRSystemUserMain {
	private BufferedReader br;
	private MemberDAO dao;
	private String cust_id;//로그인한 회원 아이디
	private boolean login;//로그인 여부(로그인:true,로그아웃:false)
	
	public WOON_HRSystemUserMain() {
		try {
			br = new BufferedReader(
					new InputStreamReader(
							     System.in));
			dao = new MemberDAO();
			callMenu();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			//자원정리
			if(br!=null)try {br.close();}
			           catch(IOException e) {}
		}
	}
	
	//메뉴
	private void callMenu()throws IOException{
		while(true) {
			System.out.print("1.로그인,2.회원가입,3.종료>");
			try {
				int no = Integer.parseInt(br.readLine());
				if(no == 1) {//로그인
					
				}else if(no == 2) {//회원가입
							
				}else if(no == 3) {//종료
					System.out.println("프로그램 종료");
					break;
				}else {
					System.out.println("잘못 입력했습니다.");
				}
			}catch(NumberFormatException e) {
				System.out.println("잘못 입력했습니다.");
			}
		}//end of while
		//로그인시 보여지는 메뉴
		while(login) {
			System.out.print(
			"1.회원 정보,2.상품 구매,3.구매 내역,4.종료>");
			try {
				int no = Integer.parseInt(
						           br.readLine());
				if(no == 1) {//회원 정보
					
				}else if(no == 2) {//상품 구매
					
				}else if(no == 3) {//구매 내역
					
				}else if(no == 4) {//종료
					//종료
					System.out.println("프로그램 종료");
					break;
				}else {
					System.out.println(
							"잘못 입력했습니다.");
				}
			}catch(NumberFormatException e) {
				System.out.println("[숫자만 입력 가능]");
			}
		}//end of while
	}
	
	public static void main(String[] args) {
		new WOON_HRSystemUserMain();
	}
}
