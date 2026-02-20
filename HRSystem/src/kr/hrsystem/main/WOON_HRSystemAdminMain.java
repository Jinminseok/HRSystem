package kr.hrsystem.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kr.hrsystem.dao.MemberDAO;

public class WOON_HRSystemAdminMain {
	private BufferedReader br;
	private MemberDAO dao;
	
	public WOON_HRSystemAdminMain() {
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
			System.out.print(
			"1.상품 등록,2.상품 목록,3.회원 목록,4.구매 목록,5.종료>");
			try {
				int no = Integer.parseInt(br.readLine());
				if(no == 1) {
					
				}else if(no == 2) {
					
				}else if(no == 3) {
					
				}else if(no == 4) {
					
				}else if(no == 5) {
					System.out.println("프로그램을 종료합니다.");
					break;
				}else {
					System.out.println("잘못 입력했습니다.");
				}
			}catch(NumberFormatException e) {
				System.out.println("[숫자만 입력 가능]");
			}
		}
	}
	
	public static void main(String[] args) {
		new WOON_HRSystemAdminMain();
	}
}


